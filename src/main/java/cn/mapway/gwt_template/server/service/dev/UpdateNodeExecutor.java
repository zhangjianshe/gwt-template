package cn.mapway.gwt_template.server.service.dev;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevNodeEntity;
import cn.mapway.gwt_template.shared.rpc.dev.UpdateNodeRequest;
import cn.mapway.gwt_template.shared.rpc.dev.UpdateNodeResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * UpdateNodeExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateNodeExecutor extends AbstractBizExecutor<UpdateNodeResponse, UpdateNodeRequest> {
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<UpdateNodeResponse> process(BizContext context, BizRequest<UpdateNodeRequest> bizParam) {
        UpdateNodeRequest request = bizParam.getData();
        log.info("UpdateNodeExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertNotNull(request.getNode(), "请输入节点信息");
        BizResult<DevNodeEntity> updateResult = projectService.saveOrUpdateNode(request.getNode());
        if (updateResult.isFailed()) {
            return updateResult.asBizResult();
        }
        UpdateNodeResponse response = new UpdateNodeResponse();
        response.setNode(request.getNode());
        return BizResult.success(response);
    }
}
