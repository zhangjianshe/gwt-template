package cn.mapway.gwt_template.server.service.docker;

import cn.mapway.gwt_template.shared.rpc.docker.WriteDockerAppResDataRequest;
import cn.mapway.gwt_template.shared.rpc.docker.WriteDockerAppResDataResponse;
import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;

import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.gwt_template.shared.AppConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import javax.annotation.Resource;

/**
 * WriteDockerAppResDataExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class WriteDockerAppResDataExecutor extends AbstractBizExecutor<WriteDockerAppResDataResponse, WriteDockerAppResDataRequest> {
    @Override
    protected BizResult<WriteDockerAppResDataResponse> process(BizContext context, BizRequest<WriteDockerAppResDataRequest> bizParam) {
        WriteDockerAppResDataRequest request = bizParam.getData();
        log.info("WriteDockerAppResDataExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        return BizResult.success(null);
    }
}
