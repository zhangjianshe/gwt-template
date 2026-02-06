package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevGroupEntity;
import cn.mapway.gwt_template.shared.db.DevGroupMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.DeleteGroupMemberRequest;
import cn.mapway.gwt_template.shared.rpc.project.DeleteGroupMemberResponse;
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
 * DeleteGroupMemberExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteGroupMemberExecutor extends AbstractBizExecutor<DeleteGroupMemberResponse, DeleteGroupMemberRequest> {
    @Resource
    ProjectService projectService;
    @Resource
    Dao dao;

    @Override
    protected BizResult<DeleteGroupMemberResponse> process(BizContext context, BizRequest<DeleteGroupMemberRequest> bizParam) {
        DeleteGroupMemberRequest request = bizParam.getData();
        log.info("DeleteGroupMemberExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        assertTrue(StringUtil.isBlank(request.getGroupName()), "没有分组名称");
        assertNotNull(request.getUserId(), "没有用户ID");

        DevGroupEntity devGroup = projectService.findGroupByName(request.getGroupName());
        assertNotNull(devGroup, "没有发现分组" + request.getGroupName());

        //检查权限
        String unauthorizedMessage = "没有授权操作分组" + request.getGroupName();
        DevGroupMemberEntity groupMember = projectService.findGroupMemberByMemberId(request.getGroupName(), user.getUser().getUserId());
        assertNotNull(groupMember, unauthorizedMessage);

        CommonPermission commonPermission = CommonPermission.fromPermission(groupMember.getPermission());
        assertTrue(commonPermission.isAdmin(), unauthorizedMessage);

        //检查成员
        DevGroupMemberEntity member = projectService.findGroupMemberByMemberId(request.getGroupName(), request.getUserId());
        if (member == null) {
            return BizResult.error(500, "成员不再分组中");
        } else {
            if (member.getOwner() != null && member.getOwner()) {
                return BizResult.error(500, "不能移除分组的创建者");
            }
            dao.deletex(DevGroupMemberEntity.class, request.getGroupName(), request.getUserId());
        }
        projectService.updateGroupMember(request.getGroupName());
        DeleteGroupMemberResponse response = new DeleteGroupMemberResponse();
        return BizResult.success(response);
    }
}
