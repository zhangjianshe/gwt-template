package cn.mapway.gwt_template.server.service.docker;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DockerAppEntity;
import cn.mapway.gwt_template.shared.rpc.docker.DeleteDirFileRequest;
import cn.mapway.gwt_template.shared.rpc.docker.DeleteDirFileResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * DeleteDirFileExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteDirFileExecutor extends AbstractBizExecutor<DeleteDirFileResponse, DeleteDirFileRequest> {
    @Resource
    DockerAppService dockerAppService;

    @Override
    protected BizResult<DeleteDirFileResponse> process(BizContext context, BizRequest<DeleteDirFileRequest> bizParam) {
        DeleteDirFileRequest request = bizParam.getData();
        log.info("DeleteDirFileExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(dockerAppService.canOperate(user), "没有授权操作");
        DockerAppEntity app = dockerAppService.findApp(request.getDockerAppId());
        assertNotNull(app, "没有应用" + request.getDockerAppId());
        String targetFile = FileCustomUtils.concatPath(app.getAbsolutePath(), request.getFilePathName());
        File file = new File(targetFile);
        if (file.exists() && file.isFile()) {
            file.delete();
        } else {
            return BizResult.error(500, "没有文件" + request.getFilePathName());
        }

        return BizResult.success(new DeleteDirFileResponse());
    }
}
