package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.DeleteProjectMemberRequest;
import cn.mapway.gwt_template.shared.rpc.project.DeleteProjectMemberResponse;
import cn.mapway.gwt_template.shared.rpc.user.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.ui.client.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * DeleteProjectMemberExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteProjectMemberExecutor extends AbstractBizExecutor<DeleteProjectMemberResponse, DeleteProjectMemberRequest> {
    @Resource
    ProjectService projectService;
    @Resource
    Dao dao;
    @Override
    protected BizResult<DeleteProjectMemberResponse> process(BizContext context, BizRequest<DeleteProjectMemberRequest> bizParam) {
        DeleteProjectMemberRequest request = bizParam.getData();
        log.info("DeleteProjectMemberExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(StringUtil.isBlank(request.getProjectId()),"没有项目名称");
        assertNotNull(request.getUserId(),"没有用户ID");

        CommonPermission permission= projectService.findUserPermissionInProject(user.getUser().getUserId(), request.getProjectId());
        assertTrue( permission.isAdmin(),"没有操作权限");

        //检查成员
        DevProjectMemberEntity member = projectService.findProjectMemberByMemberId(request.getProjectId(), request.getUserId());
        if (member == null) {
            return BizResult.error(500, "成员不再分组中");
        } else {
            if (member.getOwner() != null && member.getOwner()) {
                return BizResult.error(500, "不能移除分组的创建者");
            }
            dao.deletex(DevProjectMemberEntity.class,  request.getUserId(),request.getProjectId());
        }

        projectService.updateProjectMember(request.getProjectId());

        return BizResult.success(null);
    }
}
