package cn.mapway.gwt_template.server.service.project.res;

import cn.mapway.gwt_template.shared.rpc.project.res.ViewProjectFileRequest;
import cn.mapway.gwt_template.shared.rpc.project.res.ViewProjectFileResponse;
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
 * ViewProjectFileExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class ViewProjectFileExecutor extends AbstractBizExecutor<ViewProjectFileResponse, ViewProjectFileRequest> {
    @Override
    protected BizResult<ViewProjectFileResponse> process(BizContext context, BizRequest<ViewProjectFileRequest> bizParam) {
        ViewProjectFileRequest request = bizParam.getData();
        log.info("ViewProjectFileExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        return BizResult.success(null);
    }
}
