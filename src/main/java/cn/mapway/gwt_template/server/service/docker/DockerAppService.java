package cn.mapway.gwt_template.server.service.docker;

import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DockerAppEntity;
import cn.mapway.gwt_template.shared.rpc.file.CommonFileUploadRequest;
import cn.mapway.gwt_template.shared.rpc.file.CommonFileUploadResponse;
import cn.mapway.rbac.server.service.RbacUserService;
import cn.mapway.ui.client.IUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

@Slf4j
@Service
public class DockerAppService {
    @Resource
    Dao dao;
    @Resource
    RbacUserService rbacUserService;

    public DockerAppEntity findApp(String appId) {
        if (Strings.isBlank(appId)) {
            return null;
        }
        return dao.fetch(DockerAppEntity.class, appId);
    }


    public boolean canOperate(IUserInfo userInfo) {
        BizResult<Boolean> assignRole = rbacUserService.isAssignRole(userInfo, "", AppConstant.ROLE_DOCKER_APP_MANAGER);
        return assignRole.isSuccess() && assignRole.getData() != null && assignRole.getData();
    }

    public BizResult<CommonFileUploadResponse> saveFile(DockerAppEntity appEntity, String relativePath, CommonFileUploadRequest request) {
        String path = appEntity.getAbsolutePath();

        String targetFile = FileCustomUtils.concatPath(path, relativePath, request.getFile().getOriginalFilename());
        File parent = new File(Files.getParent(targetFile));
        if (!parent.exists() || parent.isFile()) {
            return BizResult.error(500, "确认目标路径" + relativePath);
        }

        File target = new File(targetFile);

        try {
            Files.createFileIfNoExists(target);
            Streams.writeAndClose(Streams.fileOut(targetFile), request.getFile().getInputStream());
        } catch (IOException e) {
            return BizResult.error(500, "保存文件错误:" + e.getMessage());
        }
        CommonFileUploadResponse response1 = new CommonFileUploadResponse();
        response1.setSha256("");
        response1.setMd5("");
        response1.setFileName(Files.getName(targetFile));
        try {
            response1.setMime(java.nio.file.Files.probeContentType(target.toPath()));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        response1.setRelPath(Files.getName(targetFile));
        return BizResult.success(response1);
    }
}
