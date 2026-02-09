package cn.mapway.gwt_template.server.service.webhook;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.db.WebHookInstanceEntity;
import cn.mapway.gwt_template.shared.rpc.webhook.QueryWebHookInstanceRequest;
import cn.mapway.gwt_template.shared.rpc.webhook.QueryWebHookInstanceResponse;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.QueryResult;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * QueryWebHookInstanceExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryWebHookInstanceExecutor extends AbstractBizExecutor<QueryWebHookInstanceResponse, QueryWebHookInstanceRequest> {
    @Resource
    WebHookService webHookService;

    @Override
    protected BizResult<QueryWebHookInstanceResponse> process(BizContext context, BizRequest<QueryWebHookInstanceRequest> bizParam) {
        QueryWebHookInstanceRequest request = bizParam.getData();
        log.info("QueryWebHookInstanceExecutor {}", Json.toJson(request, JsonFormat.compact()));

        // 1. Validation
        if (request == null || Strings.isBlank(request.getHookId())) {
            return BizResult.error(400, "Missing WebHook ID.");
        }

        // 2. Setup Pagination Defaults
        int page = request.getPage() == null || request.getPage() < 1 ? 1 : request.getPage();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 20 : request.getPageSize();

        // 3. Query the Log Instances
        // We want the most recent "transmissions" first
        QueryResult qr = webHookService.queryWebHookInstances(request.getHookId(), page, pageSize);

        // 4. Prepare Response
        QueryWebHookInstanceResponse response = new QueryWebHookInstanceResponse();
        response.setInstances(qr.getList(WebHookInstanceEntity.class));
        response.setTotal(qr.getPager().getRecordCount());
        response.setPage(page);
        response.setPageSize(pageSize);

        return BizResult.success(response);
    }
}
