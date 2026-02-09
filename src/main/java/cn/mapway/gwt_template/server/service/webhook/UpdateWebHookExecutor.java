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
import cn.mapway.gwt_template.shared.rpc.webhook.UpdateWebHookRequest;
import cn.mapway.gwt_template.shared.rpc.webhook.UpdateWebHookResponse;
import cn.mapway.gwt_template.shared.rpc.webhook.WebHookSourceKind;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * UpdateWebHookExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateWebHookExecutor extends AbstractBizExecutor<UpdateWebHookResponse, UpdateWebHookRequest> {
    @Resource
    WebHookService webHookService;
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<UpdateWebHookResponse> process(BizContext context, BizRequest<UpdateWebHookRequest> bizParam) {
        UpdateWebHookRequest request = bizParam.getData();
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        assertNotNull(request.getWebhook(), "没有WEB HOOK内容");

        WebHookSourceKind sourceKind = WebHookSourceKind.fromCode(request.getWebhook().getSourceKind());

        // Pass the userId from the logged-in context
        Long userId = user.getUser().getUserId();

        switch (sourceKind) {
            case HOOK_SOURCE_PROJECT:
                return checkAndSaveProjectWebHook(userId, request.getWebhook());
            case HOOK_SOURCE_WIKI:
            default:
                return BizResult.error(500, "不支持的源类型: " + sourceKind);
        }
    }

    private BizResult<UpdateWebHookResponse> checkAndSaveProjectWebHook(Long userId, WebHookEntity webhook) {

        CommonPermission permission = projectService.userProjectPermission(userId, webhook.getSourceId());
        if (!permission.isAdmin()) {
            return BizResult.error(500, "没有授权操作");
        }

        WebHookEntity saved = webHookService.saveOrUpdateWebHook(webhook);
        UpdateWebHookResponse response = new UpdateWebHookResponse();
        response.setWebhook(saved);
        return BizResult.success(response);
    }
}
