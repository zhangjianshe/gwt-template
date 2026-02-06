package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevGroupEntity;
import cn.mapway.gwt_template.shared.db.DevGroupMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateGroupMemberRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateGroupMemberResponse;
import cn.mapway.gwt_template.shared.rpc.user.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.ui.client.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;

/**
 * UpdateGroupMemberExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateGroupMemberExecutor extends AbstractBizExecutor<UpdateGroupMemberResponse, UpdateGroupMemberRequest> {
    @Resource
    ProjectService projectService;
    @Resource
    Dao dao;

    @Override
    protected BizResult<UpdateGroupMemberResponse> process(BizContext context, BizRequest<UpdateGroupMemberRequest> bizParam) {
        UpdateGroupMemberRequest request = bizParam.getData();
        log.info("UpdateGroupMemberExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(StringUtil.isBlank(request.getGroupName()),"没有分组名称");
        assertNotNull(request.getUserId(),"没有用户ID");

        DevGroupEntity devGroup= projectService.findGroupByName(request.getGroupName());
        assertNotNull(devGroup,"没有发现分组"+request.getGroupName());

        //检查权限
        String unauthorizedMessage="没有授权操作分组"+request.getGroupName();
        DevGroupMemberEntity groupMember= projectService.findGroupMemberByMemberId(request.getGroupName(), user.getUser().getUserId());
        assertNotNull(groupMember,unauthorizedMessage);

        CommonPermission commonPermission=CommonPermission.fromPermission(groupMember.getPermission());
        assertTrue(commonPermission.isAdmin(),unauthorizedMessage);

        //检查成员
        DevGroupMemberEntity member= projectService.findGroupMemberByMemberId(request.getGroupName(), request.getUserId());
        if(member==null){
            member= new DevGroupMemberEntity();
            member.setGroupName(request.getGroupName());
            member.setUserId(request.getUserId());
            member.setCreateTime(new Timestamp(System.currentTimeMillis()));
            member.setOwner(false);
            member.setPermission(CommonPermission.fromPermission(request.getPermission()).getPermission());
            dao.insert(member);
        }
        else {
            member.setOwner(null);
            member.setPermission(CommonPermission.fromPermission(request.getPermission()).getPermission());
            dao.updateIgnoreNull(member);
        }
        projectService.updateGroupMember(request.getGroupName());

        member= projectService.findGroupMemberByMemberId(request.getGroupName(), request.getUserId());
        UpdateGroupMemberResponse response = new UpdateGroupMemberResponse();
        response.setMember(member);
        return BizResult.success(response);
    }
}
