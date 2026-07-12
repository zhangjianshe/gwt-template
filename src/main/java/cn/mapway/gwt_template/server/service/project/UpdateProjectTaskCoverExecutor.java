package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskCoverRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskCoverResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.img.Images;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * UpdateProjectTaskCoverExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateProjectTaskCoverExecutor extends AbstractBizExecutor<UpdateProjectTaskCoverResponse, UpdateProjectTaskCoverRequest> {
    @Resource
    ProjectService projectService;
    @Resource
    Dao dao;

    @Override
    protected BizResult<UpdateProjectTaskCoverResponse> process(BizContext context, BizRequest<UpdateProjectTaskCoverRequest> bizParam) {
        UpdateProjectTaskCoverRequest request = bizParam.getData();
        log.info("UpdateProjectTaskCoverExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        DevProjectTaskEntity task = projectService.findTask(request.getTaskId());
        assertNotNull(task, "没有会议信息");
        CommonPermission userPermissionInProject = projectService.findUserPermissionInProject(user.getUser().getUserId(), task.getProjectId());
        assertTrue(projectService.isChargerOfTask(user.getUser().getUserId(), request.getTaskId())
                || userPermissionInProject.isSuper()
                || userPermissionInProject.isSecretary(), "没有权限操作");

        if (request.getClearCover() == null || !request.getClearCover()) {
            File file = new File(projectService.getTaskAttachmentRoot(task) + "/" + request.getPicturePath());
            if (!file.exists() || !file.isFile()) {
                log.warn("[PROJECT] 更新人物封面提供不合适的图片{}", request.getPicturePath());
                return BizResult.error(500, "更新人物封面提供不合适的图片");
            }
            try {
                BufferedImage image = ImageIO.read(file);
                BufferedImage scale = Images.scale(image, 200, -1);
                String name = Lang.md5(file).substring(0, 6);
                String coverPathName = "/upload/task/cover/" + task.getId().substring(0, 3) + "/" + name + ".png";
                String targetPath = FileCustomUtils.concatPath(
                        projectService.systemConfigService.getUploadRoot(),
                        coverPathName);
                Files.createFileIfNoExists(targetPath);
                log.info("[PROJECT] write cover to {}", targetPath);
                ImageIO.write(scale, "png", new File(targetPath));
                DevProjectTaskEntity temp = new DevProjectTaskEntity();
                temp.setId(task.getId());
                temp.setCover(coverPathName);
                dao.updateIgnoreNull(temp);
            } catch (Exception e) {
                log.error("[PROJECT] 转换图片错误 {} {}", request.getPicturePath(), e.getMessage());
                return BizResult.error(500, "转换图片错误" + e.getMessage());
            }
        } else {
            // clear cover
            DevProjectTaskEntity temp = new DevProjectTaskEntity();
            temp.setId(task.getId());
            temp.setCover("");
            dao.updateIgnoreNull(temp);
        }

        UpdateProjectTaskCoverResponse response = new UpdateProjectTaskCoverResponse();
        response.setTask(dao.fetch(DevProjectTaskEntity.class, task.getId()));
        return BizResult.success(response);
    }
}
