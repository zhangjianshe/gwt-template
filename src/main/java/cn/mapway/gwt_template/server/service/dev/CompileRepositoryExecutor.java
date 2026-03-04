package cn.mapway.gwt_template.server.service.dev;

import cn.mapway.gwt_template.shared.rpc.dev.CompileRepositoryRequest;
import cn.mapway.gwt_template.shared.rpc.dev.CompileRepositoryResponse;
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

/**
 * CompileProjectExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class CompileRepositoryExecutor extends AbstractBizExecutor<CompileRepositoryResponse, CompileRepositoryRequest> {
    @Override
    protected BizResult<CompileRepositoryResponse> process(BizContext context, BizRequest<CompileRepositoryRequest> bizParam) {
        CompileRepositoryRequest request = bizParam.getData();
        log.info("CompileProjectExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        return BizResult.success(null);
    }
}
