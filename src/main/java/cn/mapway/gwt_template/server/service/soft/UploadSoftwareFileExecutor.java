package cn.mapway.gwt_template.server.service.soft;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.config.AppConfig;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.SysSoftwareEntity;
import cn.mapway.gwt_template.shared.db.SysSoftwareFileEntity;
import cn.mapway.gwt_template.shared.rpc.soft.UploadSoftwareFileRequest;
import cn.mapway.gwt_template.shared.rpc.soft.UploadSoftwareFileResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;

/**
 * UploadSoftwareFileExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UploadSoftwareFileExecutor extends AbstractBizExecutor<UploadSoftwareFileResponse, UploadSoftwareFileRequest> {
    @Resource
    Dao dao;
    @Resource
    AppConfig appConfig;

    @Override
    protected BizResult<UploadSoftwareFileResponse> process(BizContext context, BizRequest<UploadSoftwareFileRequest> bizParam) {
        UploadSoftwareFileRequest request = bizParam.getData();
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getToken()), "没有授权操作");
        SysSoftwareEntity software = dao.fetch(SysSoftwareEntity.class, Cnd.where(SysSoftwareEntity.FLD_TOKEN, "=", request.getToken()));
        assertTrue(software!=null, "没有软件信息" + request.getToken());
        assertTrue(Strings.isNotBlank(request.getOs()), "没有OS");
        assertTrue(Strings.isNotBlank(request.getArch()), "没有Arch");
        assertTrue(Strings.isNotBlank(request.getVersion()), "没有Version");
        assertTrue(Strings.isNotBlank(request.getName()), "没有Name");

        String targetPath = appConfig.getUploadPath() + "/software/";
        Files.createDirIfNoExists(targetPath);

        String versionPath = appConfig.getUploadPath() + "/software/"+software.getId()+"/"+request.getVersion();
        Files.createDirIfNoExists(versionPath);

        if (request.getFile() == null) {
            return BizResult.error(500, "没有文件 file 字段");
        }

        String name = request.getFile().getOriginalFilename();
        String targetFileName = versionPath + "/" + name;

        try {
            Files.write(targetFileName, request.getFile().getInputStream());
        } catch (IOException e) {
            return BizResult.error(500, e.getMessage());
        }
        SysSoftwareFileEntity fileEntity = new SysSoftwareFileEntity();
        fileEntity.setId(R.UU16());
        fileEntity.setName(request.getName());
        fileEntity.setSoftwareId(software.getId());
        fileEntity.setSize(new File(targetFileName).length());
        fileEntity.setOs(request.getOs());
        fileEntity.setSummary(request.getSummary());
        fileEntity.setArch(request.getArch());
        fileEntity.setVersion(request.getVersion());
        fileEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
        fileEntity.setLocation("/software/"+targetFileName.substring(targetPath.length() + 1));
        dao.insert(fileEntity);
        UploadSoftwareFileResponse response = new UploadSoftwareFileResponse();
        response.setUrl("/upload/"+fileEntity.getLocation());
        return BizResult.success(response);
    }
}
