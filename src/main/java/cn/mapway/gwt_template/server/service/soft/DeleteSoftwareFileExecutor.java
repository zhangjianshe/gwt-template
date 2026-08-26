package cn.mapway.gwt_template.server.service.soft;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.config.SystemConfigService;
import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.SysSoftwareFileEntity;
import cn.mapway.gwt_template.shared.rpc.soft.DeleteSoftwareFileRequest;
import cn.mapway.gwt_template.shared.rpc.soft.DeleteSoftwareFileResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.server.service.RbacUserService;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * DeleteSoftwareFileExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteSoftwareFileExecutor extends AbstractBizExecutor<DeleteSoftwareFileResponse, DeleteSoftwareFileRequest> {
    @Resource
    Dao dao;
    @Resource
    SystemConfigService systemConfigService;

    @Resource
    RbacUserService rbacUserService;

    @Override
    protected BizResult<DeleteSoftwareFileResponse> process(BizContext context, BizRequest<DeleteSoftwareFileRequest> bizParam) {
        DeleteSoftwareFileRequest request = bizParam.getData();
        log.info("DeleteSoftwareFileExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getFileId()), "没有文件ID");
        BizResult<Boolean> canDelete = rbacUserService.isAssignRole(user, "", AppConstant.ROLE_SOFTWARE_MANAGER);
        assertTrue(canDelete.isSuccess() && canDelete.getData(),"没有授权操作");

        SysSoftwareFileEntity fileEntity = dao.fetch(SysSoftwareFileEntity.class, request.getFileId());
        assertNotNull(fileEntity, "没有文件信息" + request.getFileId());

        // 删除物理文件
        String location = fileEntity.getLocation();
        if (Strings.isNotBlank(location)) {
            String targetFile = FileCustomUtils.concatPath(systemConfigService.getUploadRoot(), location);
            File file = new File(targetFile);
            if (file.exists() && file.isFile()) {
                file.delete();
            }
        }

        dao.delete(SysSoftwareFileEntity.class, request.getFileId());
        return BizResult.success(new DeleteSoftwareFileResponse());
    }
}
