package cn.mapway.gwt_template.server.service.repository;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevRepositoryEntity;
import cn.mapway.gwt_template.shared.db.DevRepositoryMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectPermissionKind;
import cn.mapway.gwt_template.shared.rpc.repository.TransferRepositoryRequest;
import cn.mapway.gwt_template.shared.rpc.repository.TransferRepositoryResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.trans.Trans;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * TransferRepositoryExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class TransferRepositoryExecutor extends AbstractBizExecutor<TransferRepositoryResponse, TransferRepositoryRequest> {
    @Resource
    RepositoryService repositoryService;
    @Resource
    Dao dao;

    @Override
    protected BizResult<TransferRepositoryResponse> process(BizContext context, BizRequest<TransferRepositoryRequest> bizParam) {
        TransferRepositoryRequest request = bizParam.getData();
        log.info("TransferRepositoryExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertNotNull(request.getTargetUserId(), "请设置目标用户");
        assertTrue(Strings.isNotBlank(request.getRepositoryId()), "请设置要转移的仓库ID");
        DevRepositoryEntity repository = repositoryService.findRepositoryById(request.getRepositoryId());
        assertNotNull(repository, "没有找到您要转移的仓库");
        if (request.getTargetUserId().equals(repository.getUserId())) {
            return BizResult.error(500, "您已经是该仓库的拥有者 无须转移");
        }

        CommonPermission operatorPermission = repositoryService.findUserPermissionInRepository(user.getUser().getUserId(), request.getRepositoryId());
        assertTrue(operatorPermission.isOwner() || user.isAdmin(), "只有仓库拥有者或者系统管理员有转移仓库权限");

        CommonPermission targetPermission = CommonPermission.all();

        Trans.exec(() -> {
            repositoryService.updateUserPermissionInRepository(request.getTargetUserId(), request.getRepositoryId(), targetPermission);

            //更新老成员的权限
            DevRepositoryMemberEntity member = repositoryService.findRepositoryMemberByMemberId(request.getRepositoryId(), repository.getUserId());
            member.setOwner(false);
            CommonPermission commonPermission = CommonPermission.from(member.getPermission());
            commonPermission.set(ProjectPermissionKind.OWNER, false);
            member.setPermission(commonPermission.toString());
            dao.updateIgnoreNull(member);

            //更新仓库的创建者信息
            RbacUserEntity rbacUser = dao.fetch(RbacUserEntity.class, request.getTargetUserId());
            DevRepositoryEntity temp = new DevRepositoryEntity();
            temp.setUserId(request.getTargetUserId());
            temp.setId(repository.getId());
            temp.setOwnerName(rbacUser.getUserName());
            dao.updateIgnoreNull(temp);

        });


        TransferRepositoryResponse response = new TransferRepositoryResponse();
        return BizResult.success(response);
    }
}
