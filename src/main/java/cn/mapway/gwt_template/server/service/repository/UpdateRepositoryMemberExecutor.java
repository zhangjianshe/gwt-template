package cn.mapway.gwt_template.server.service.repository;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevRepositoryMemberEntity;
import cn.mapway.gwt_template.shared.db.VwRepositoryMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.repository.UpdateRepositoryMemberRequest;
import cn.mapway.gwt_template.shared.rpc.repository.UpdateRepositoryMemberResponse;
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
 * UpdateRepositoryMemberExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateRepositoryMemberExecutor extends AbstractBizExecutor<UpdateRepositoryMemberResponse, UpdateRepositoryMemberRequest> {
    @Resource
    RepositoryService repositoryService;
    @Resource
    Dao dao;

    @Override
    protected BizResult<UpdateRepositoryMemberResponse> process(BizContext context, BizRequest<UpdateRepositoryMemberRequest> bizParam) {
        UpdateRepositoryMemberRequest request = bizParam.getData();
        log.info("UpdateProjectMemberExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getRepositoryId()), "没有项目ID");
        assertNotNull(request.getUserId(), "没有用户ID");

        CommonPermission permission = repositoryService.findUserPermissionInRepository(user.getUser().getUserId(), request.getRepositoryId());
        assertTrue(permission.isSuper(), "没有操作权限");

        //检查成员
        DevRepositoryMemberEntity member = repositoryService.findRepositoryMemberByMemberId(request.getRepositoryId(), request.getUserId());
        if (member == null) {
            member = new DevRepositoryMemberEntity();
            member.setRepositoryId(request.getRepositoryId());
            member.setOwner(false);
            member.setUserId(request.getUserId());
            member.setCreateTime(new Timestamp(System.currentTimeMillis()));
            member.setPermission(request.getPermission());
            dao.insert(member);
        } else {
            member.setPermission(CommonPermission.from(request.getPermission()).toString());
            dao.updateIgnoreNull(member);
        }
        repositoryService.updateProjectMember(request.getRepositoryId());

        VwRepositoryMemberEntity memberView = repositoryService.findProjectMemberViewByMemberId(request.getRepositoryId(), request.getUserId());

        UpdateRepositoryMemberResponse response = new UpdateRepositoryMemberResponse();
        response.setMember(memberView);
        return BizResult.success(response);
    }
}
