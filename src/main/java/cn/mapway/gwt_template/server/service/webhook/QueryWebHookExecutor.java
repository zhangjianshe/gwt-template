package cn.mapway.gwt_template.server.service.webhook;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.db.WebHookEntity;
import cn.mapway.gwt_template.shared.rpc.webhook.QueryWebHookRequest;
import cn.mapway.gwt_template.shared.rpc.webhook.QueryWebHookResponse;
import cn.mapway.gwt_template.shared.rpc.webhook.WebHookSourceKind;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryWebHookExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryWebHookExecutor extends AbstractBizExecutor<QueryWebHookResponse, QueryWebHookRequest> {

    @Resource
    WebHookService webHookService;

    @Override
    protected BizResult<QueryWebHookResponse> process(BizContext context, BizRequest<QueryWebHookRequest> bizParam) {
        QueryWebHookRequest request = bizParam.getData();
        log.info("QueryWebHookExecutor {}", Json.toJson(request, JsonFormat.compact()));

        // 1. Validate Input
        if (request == null || Strings.isBlank(request.getSourceId())) {
            return BizResult.error(400, "Missing Source ID for webhook query.");
        }

        // 2. Resolve the Kind (e.g., PROJECT, WIKI)
        WebHookSourceKind kind = WebHookSourceKind.fromCode(request.getWebhookSourceKind());

        // 3. Query the Database via the Service
        List<WebHookEntity> hooks = webHookService.queryWebHookList(kind, request.getSourceId());

        // 4. Return the List to the GWT UI
        QueryWebHookResponse response = new QueryWebHookResponse();
        response.setHooks(hooks);

        return BizResult.success(response);
    }
}