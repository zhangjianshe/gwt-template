package cn.mapway.gwt_template.server.service.webhook;

import cn.mapway.gwt_template.server.service.git.GitPushPayload;
import cn.mapway.gwt_template.shared.db.WebHookEntity;
import cn.mapway.gwt_template.shared.db.WebHookInstanceEntity;
import cn.mapway.gwt_template.shared.rpc.webhook.WebHookSourceKind;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.QueryResult;
import org.nutz.dao.pager.Pager;
import org.nutz.http.Header;
import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.Tasks;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.List;

@Slf4j
@Service
public class WebHookService {
    @Resource
    Dao dao;

    /**
     * 查询 WEB HOOK 定义列表
     *
     * @param source
     * @param sourceId
     * @return
     */
    public List<WebHookEntity> queryWebHookList(WebHookSourceKind source, String sourceId) {
        Cnd where = Cnd.where(WebHookEntity.FLD_SOURCE_KIND, "=", source.getCode()).and(WebHookEntity.FLD_SOURCE_ID, "=", sourceId);
        where.desc(WebHookEntity.FLD_CREATE_TIME);
        return dao.query(WebHookEntity.class, where);
    }

    /**
     * 保存或者更新WebHook
     *
     * @param webHookEntity
     * @return
     */
    public WebHookEntity saveOrUpdateWebHook(WebHookEntity webHookEntity) {
        if (Strings.isBlank(webHookEntity.getId())) {
            webHookEntity.setId(R.UU16());
            return dao.insert(webHookEntity);
        } else {
            dao.updateIgnoreNull(webHookEntity);
            return dao.fetch(WebHookEntity.class, webHookEntity.getId());
        }
    }

    /**
     * 删除 WebHookId
     *
     * @param hookId
     */
    public void deleteWebHook(String hookId) {
        dao.clear(WebHookInstanceEntity.class, Cnd.where(WebHookInstanceEntity.FLD_WEBHOOK_ID, "=", hookId));
        dao.delete(WebHookEntity.class, hookId);
    }

    public void saveOrUpdateWebHookInstance(WebHookInstanceEntity instance) {
        if (Strings.isBlank(instance.getId())) {
            if (Strings.isBlank(instance.getWebhookId())) {
                log.error("[WEBHOOK] 没有WEBHOOK ID");
                return;
            }
            instance.setId(R.UU16());
            instance.setCreateTime(new Timestamp(System.currentTimeMillis()));
            dao.insert(instance);
        } else {
            instance.setCreateTime(null);
            instance.setWebhookId(null);
            dao.updateIgnoreNull(instance);
        }
    }

    public WebHookEntity fetchWebHook(String hookId) {
        return dao.fetch(WebHookEntity.class, hookId);
    }

    public QueryResult queryWebHookInstances(String hookId, int page, int pageSize) {
        Cnd cnd = Cnd.where(WebHookInstanceEntity.FLD_WEBHOOK_ID, "=", hookId);
        cnd.desc(WebHookInstanceEntity.FLD_CREATE_TIME); // Latest first

        Pager pager = dao.createPager(page, pageSize);
        List<WebHookInstanceEntity> list = dao.query(WebHookInstanceEntity.class, cnd, pager);
        pager.setRecordCount(dao.count(WebHookInstanceEntity.class, cnd));

        return new QueryResult(list, pager);
    }

    /**
     * Finds all active hooks for the project and dispatches the payload.
     */
    public void triggerWebHooks(Integer sourceKind, String sourceId, String event, Object payload) {
        // 1. Fetch all active hooks for this project/mission
        List<WebHookEntity> hooks = dao.query(WebHookEntity.class,
                Cnd.where(WebHookEntity.FLD_SOURCE_ID, "=", sourceId)
                        .and(WebHookEntity.FLD_SOURCE_KIND, "=", sourceKind)
                        .and("active", "=", true));

        if (hooks.isEmpty()) {
            log.debug("No active webhooks found for source: {}", sourceId);
            return;
        }

        String jsonPayload = Json.toJson(payload, JsonFormat.compact());

        for (WebHookEntity hook : hooks) {
            // 2. Simple Filter Check (Optional)
            // If the hook has a filter like "refs/heads/main", we check the payload
            if (!isAccepted(hook, payload)) {
                continue;
            }

            Tasks.getTaskScheduler().submit(() -> {
                // 3. Execute the HTTP Request
                sendRequest(hook, jsonPayload);
            });

        }
    }

    private void sendRequest(WebHookEntity hook, String body) {
        WebHookInstanceEntity instance = new WebHookInstanceEntity();
        instance.setWebhookId(hook.getId());
        instance.setCreateTime(new Timestamp(System.currentTimeMillis()));
        instance.setRequestBody(body);

        long start = System.currentTimeMillis();
        try {
            // Using Nutz Http or Apache HttpClient
            Header header = Header.create();
            header.set("Content-Type", hook.getContentType());
            if (Strings.isNotBlank(hook.getAuthorizeKey())) {
                header.set("Authorization", hook.getAuthorizeKey());
            }
            String[] lines = Strings.split(hook.getHeaders(), false, '\r', '\n');
            if (lines != null) {
                for (String line : lines) {
                    String[] segs = Strings.split(line, false, false, '=');
                    if (segs != null && segs.length == 2) {
                        header.set(segs[0], segs[1]);
                    }
                }
            }

            Response response = Http.post3(hook.getTargetUrl(), body, header, 5000); // 5s timeout

            instance.setResponseCode(response.getStatus());
            instance.setResponseBody(response.getContent());
            instance.setDuration((System.currentTimeMillis() - start));
            log.info("Webhook success: {}", hook.getTargetUrl());

        } catch (Exception e) {
            instance.setResponseCode(500);
            instance.setResponseBody("Error: " + e.getMessage());
            log.error("Webhook failed for URL: {}", hook.getTargetUrl(), e);
        }

        // 4. Log the result and update telemetry count
        instance.setId(R.UU16());
        dao.insert(instance);
        dao.update(WebHookEntity.class, Chain.makeSpecial("activeCount", "+1"),
                Cnd.where("id", "=", hook.getId()));
    }

    private boolean isAccepted(WebHookEntity hook, Object payload) {
        if (Strings.isBlank(hook.getSourceFilter())) return true;

        if (payload instanceof GitPushPayload) {
            String currentRef = ((GitPushPayload) payload).getRef();
            String filter = hook.getSourceFilter();

            // Example: allow simple wildcard matching
            if (filter.contains("*")) {
                String regex = filter.replace("*", ".*");
                return currentRef.matches(regex);
            }
            return currentRef.equals(filter);
        }
        return false;
    }
}
