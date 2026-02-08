package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.SysUserKeyEntity;
import cn.mapway.gwt_template.shared.rpc.project.DeleteUserKeyRequest;
import cn.mapway.gwt_template.shared.rpc.project.DeleteUserKeyResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * DeleteUserKeyExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteUserKeyExecutor extends AbstractBizExecutor<DeleteUserKeyResponse, DeleteUserKeyRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<DeleteUserKeyResponse> process(BizContext context, BizRequest<DeleteUserKeyRequest> bizParam) {
        DeleteUserKeyRequest request = bizParam.getData();
        log.info("DeleteUserKeyExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        assertTrue(Strings.isNotBlank(request.getKey()), "请提供要删除的公钥");
        SysUserKeyEntity fetch = dao.fetch(SysUserKeyEntity.class, request.getKey());
        if (fetch == null) {
            return BizResult.error(500, "没有要删除的公钥");
        }
        if (!fetch.getUserId().equals(user.getUser().getUserId())) {
            return BizResult.error(500, "没有权限删除该公钥");
        }

        dao.delete(SysUserKeyEntity.class, request.getKey());
        return BizResult.success(new DeleteUserKeyResponse());
    }
}
