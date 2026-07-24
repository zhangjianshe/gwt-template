package cn.mapway.gwt_template.server.service.docker;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.docker.RestartDockerAppRequest;
import cn.mapway.gwt_template.shared.rpc.docker.RestartDockerAppResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * RestartDockerAppExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class RestartDockerAppExecutor extends AbstractBizExecutor<RestartDockerAppResponse, RestartDockerAppRequest> {
    @Resource
    DockerAppService dockerAppService;
    @Override
    protected BizResult<RestartDockerAppResponse> process(BizContext context, BizRequest<RestartDockerAppRequest> bizParam) {
        RestartDockerAppRequest request = bizParam.getData();
        log.info("RestartDockerAppExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(dockerAppService.canOperate(user),"没有授权操作");

        return BizResult.success(null);
    }
}
