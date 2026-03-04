package cn.mapway.gwt_template.server.service.repository;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevRepositoryMemberEntity;
import cn.mapway.gwt_template.shared.rpc.repository.DeleteRepositoryMemberRequest;
import cn.mapway.gwt_template.shared.rpc.repository.DeleteRepositoryMemberResponse;
import cn.mapway.gwt_template.shared.rpc.user.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * DeleteRepositoryMemberExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteRepositoryMemberExecutor extends AbstractBizExecutor<DeleteRepositoryMemberResponse, DeleteRepositoryMemberRequest> {
    @Resource
    RepositoryService repositoryService;
    @Resource
    Dao dao;

    @Override
    protected BizResult<DeleteRepositoryMemberResponse> process(BizContext context, BizRequest<DeleteRepositoryMemberRequest> bizParam) {
        DeleteRepositoryMemberRequest request = bizParam.getData();
        log.info("DeleteRepositoryMemberExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getRepositoryId()), "没有仓库ID");
        assertNotNull(request.getUserId(), "没有用户ID");

        CommonPermission permission = repositoryService.findUserPermissionInRepository(user.getUser().getUserId(), request.getRepositoryId());
        assertTrue(permission.isAdmin(), "没有操作权限");

        //检查成员
        DevRepositoryMemberEntity member = repositoryService.findProjectMemberByMemberId(request.getRepositoryId(), request.getUserId());
        if (member == null) {
            return BizResult.error(500, "成员不再分组中");
        } else {
            if (member.getOwner() != null && member.getOwner()) {
                return BizResult.error(500, "不能移除分组的创建者");
            }
            dao.deletex(DevRepositoryMemberEntity.class, request.getUserId(), request.getRepositoryId());
        }

        repositoryService.updateProjectMember(request.getRepositoryId());

        return BizResult.success(null);
    }
}
