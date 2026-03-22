package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.project.UploadProjectAttachmentRequest;
import cn.mapway.gwt_template.shared.rpc.project.UploadProjectAttachmentResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

/**
 * UploadProjectAttachmentExecutor
 * 向项目的任务上传文件附件
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UploadProjectAttachmentExecutor extends AbstractBizExecutor<UploadProjectAttachmentResponse, UploadProjectAttachmentRequest> {
    @Override
    protected BizResult<UploadProjectAttachmentResponse> process(BizContext context, BizRequest<UploadProjectAttachmentRequest> bizParam) {
        UploadProjectAttachmentRequest request = bizParam.getData();
        log.info("UploadProjectAttachmentExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        return BizResult.success(null);
    }
}
