package cn.mapway.gwt_template.server.service.project;

import cn.mapway.gwt_template.shared.rpc.project.UploadProjectFilesRequest;
import cn.mapway.gwt_template.shared.rpc.project.UploadProjectFilesResponse;
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
 * UploadProjectFilesExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UploadProjectFilesExecutor extends AbstractBizExecutor<UploadProjectFilesResponse, UploadProjectFilesRequest> {
    @Override
    protected BizResult<UploadProjectFilesResponse> process(BizContext context, BizRequest<UploadProjectFilesRequest> bizParam) {
        UploadProjectFilesRequest request = bizParam.getData();
        log.info("UploadProjectFilesExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        return BizResult.success(null);
    }
}
