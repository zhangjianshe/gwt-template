package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.SysUserKeyEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateUserKeyRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateUserKeyResponse;
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
 * UpdateUserKeyExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateUserKeyExecutor extends AbstractBizExecutor<UpdateUserKeyResponse, UpdateUserKeyRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<UpdateUserKeyResponse> process(BizContext context, BizRequest<UpdateUserKeyRequest> bizParam) {
        UpdateUserKeyRequest request = bizParam.getData();
        log.info("UpdateUserKeyExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(request.getKey() != null && Strings.isNotBlank(request.getKey().getKey()), "请提供公钥");

        SysUserKeyEntity fetch = dao.fetch(SysUserKeyEntity.class, request.getKey().getKey());
        if (fetch == null) {
            // create
            SysUserKeyEntity key = request.getKey();
            key.setUserId(user.getUser().getUserId());
            key.setCreateTime(new Timestamp(System.currentTimeMillis()));
            if (key.getExpiredTime() == null || key.getExpiredTime() <= 0) {
                key.setExpiredTime(0L);
            }
            dao.insert(key);
        } else {
            if (fetch.getUserId().equals(user.getId())) {
                // update
                SysUserKeyEntity key = request.getKey();
                key.setCreateTime(null);
                if (key.getExpiredTime() == null || key.getExpiredTime() <= 0) {
                    key.setExpiredTime(0L);
                }
                dao.updateIgnoreNull(key);
            } else {
                return BizResult.error(500, "该公钥已被用户绑定");
            }
        }


        return BizResult.success(new UpdateUserKeyResponse());
    }
}
