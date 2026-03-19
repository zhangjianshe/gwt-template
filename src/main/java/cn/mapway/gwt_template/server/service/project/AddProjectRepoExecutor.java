package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectRepoEntity;
import cn.mapway.gwt_template.shared.db.DevRepositoryMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.AddProjectRepoRequest;
import cn.mapway.gwt_template.shared.rpc.project.AddProjectRepoResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.trans.Trans;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;

/**
 * AddProjectRepoExecutor
 * 向项目中关联一个仓库·
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class AddProjectRepoExecutor extends AbstractBizExecutor<AddProjectRepoResponse, AddProjectRepoRequest> {
    @Resource
    Dao dao;
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<AddProjectRepoResponse> process(BizContext context, BizRequest<AddProjectRepoRequest> bizParam) {
        AddProjectRepoRequest request = bizParam.getData();
        log.info("AddProjectRepoExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getProjectId()) && Strings.isNotBlank(request.getRepoId()), "需要项目ID和仓库ID");
        CommonPermission permission = projectService.findUserPermissionInProject(user.getUser().getUserId(), request.getProjectId());
        assertTrue(permission.isSuper(), "只有创建者和管理员可以操作");
        DevProjectRepoEntity fetchx = dao.fetchx(DevProjectRepoEntity.class, request.getProjectId(), request.getRepoId());
        if (fetchx != null) {
            return BizResult.success(new AddProjectRepoResponse());
        }

        Trans.exec(() -> {
            DevProjectRepoEntity entity = new DevProjectRepoEntity();
            entity.setProjectId(request.getProjectId());
            entity.setRepositoryId(request.getRepoId());
            entity.setCreateTime(new Timestamp(System.currentTimeMillis()));
            dao.insert(entity);

            DevRepositoryMemberEntity member = dao.fetchx(DevRepositoryMemberEntity.class, user.getUser().getUserId(), request.getRepoId());
            if (member == null) {
                member = new DevRepositoryMemberEntity();
                member.setUserId(user.getUser().getUserId());
                member.setOwner(true);
                member.setPermission(CommonPermission.owner().toString());
                member.setCreateTime(new Timestamp(System.currentTimeMillis()));
                member.setRepositoryId(request.getRepoId());
                dao.insert(member);
            } else {
                member.setOwner(false);
                member.setPermission(CommonPermission.owner().toString());
                dao.update(member);
            }
        });

        return BizResult.success(new AddProjectRepoResponse());
    }
}
