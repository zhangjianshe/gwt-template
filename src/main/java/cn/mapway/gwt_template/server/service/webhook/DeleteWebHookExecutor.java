package cn.mapway.gwt_template.server.service.webhook;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.WebHookEntity;
import cn.mapway.gwt_template.shared.rpc.user.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.gwt_template.shared.rpc.webhook.DeleteWebHookRequest;
import cn.mapway.gwt_template.shared.rpc.webhook.DeleteWebHookResponse;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * DeleteWebHookExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteWebHookExecutor extends AbstractBizExecutor<DeleteWebHookResponse, DeleteWebHookRequest> {

    @Resource
    WebHookService webHookService;

    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<DeleteWebHookResponse> process(BizContext context, BizRequest<DeleteWebHookRequest> bizParam) {
        DeleteWebHookRequest request = bizParam.getData();
        log.info("DeleteWebHookExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        // 1. Validation
        if (request == null || Strings.isBlank(request.getHookId())) {
            return BizResult.error(400, "Missing WebHook ID");
        }

        // 2. Security Check
        // We must fetch the hook first to see what it belongs to
        WebHookEntity hook = webHookService.fetchWebHook(request.getHookId());
        if (hook == null) {
            return BizResult.success(new DeleteWebHookResponse()); // Already gone
        }

        // Only project admins should be able to delete hooks
        CommonPermission permission = projectService.userProjectPermission(
                user.getUser().getUserId(), hook.getSourceId());

        if (!permission.isAdmin()) {
            return BizResult.error(403, "Forbidden: Only admins can remove data hooks.");
        }

        // 3. Execution
        webHookService.deleteWebHook(request.getHookId());

        return BizResult.success(new DeleteWebHookResponse());
    }
}
