package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectMemberEntity;
import cn.mapway.gwt_template.shared.db.VwProjectMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectMemberRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectMemberResponse;
import cn.mapway.gwt_template.shared.rpc.user.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;

/**
 * UpdateProjectMemberExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateProjectMemberExecutor extends AbstractBizExecutor<UpdateProjectMemberResponse, UpdateProjectMemberRequest> {
    @Resource
    ProjectService projectService;
    @Resource
    Dao dao;

    @Override
    protected BizResult<UpdateProjectMemberResponse> process(BizContext context, BizRequest<UpdateProjectMemberRequest> bizParam) {
        UpdateProjectMemberRequest request = bizParam.getData();
        log.info("UpdateProjectMemberExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getProjectId()), "没有项目ID");
        assertNotNull(request.getUserId(), "没有用户ID");

        CommonPermission permission = projectService.findUserPermissionInProject(user.getUser().getUserId(), request.getProjectId());
        assertTrue(permission.isAdmin(), "没有操作权限");

        //检查成员
        DevProjectMemberEntity member = projectService.findProjectMemberByMemberId(request.getProjectId(), request.getUserId());
        if (member == null) {
            member = new DevProjectMemberEntity();
            member.setProjectId(request.getProjectId());
            member.setOwner(false);
            member.setUserId(request.getUserId());
            member.setCreateTime(new Timestamp(System.currentTimeMillis()));
            member.setPermission(request.getPermission());
            dao.insert(member);
        } else {
            member.setPermission(CommonPermission.fromPermission(request.getPermission()).getPermission());
            dao.updateIgnoreNull(member);
        }
        projectService.updateProjectMember(request.getProjectId());

        VwProjectMemberEntity memberView = projectService.findProjectMemberViewByMemberId(request.getProjectId(), request.getUserId());

        UpdateProjectMemberResponse response = new UpdateProjectMemberResponse();
        response.setMember(memberView);
        return BizResult.success(response);
    }
}
