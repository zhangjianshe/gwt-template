package cn.mapway.gwt_template.server.service.user;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.ldap.LdapService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.user.UpdateUserInfoRequest;
import cn.mapway.gwt_template.shared.rpc.user.UpdateUserInfoResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.shared.RbacConstant;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Base64;
import java.util.Date;

/**
 * UpdateUserInfoExecutor
 * TODO 目前这个方法只能够更新 本地用户 不能更新 LDAP 需要编码
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateUserInfoExecutor extends AbstractBizExecutor<UpdateUserInfoResponse, UpdateUserInfoRequest> {
    @Resource
    Dao dao;
    @Resource
    LdapService ldapService;

    @Override
    protected BizResult<UpdateUserInfoResponse> process(BizContext context, BizRequest<UpdateUserInfoRequest> bizParam) {
        UpdateUserInfoRequest request = bizParam.getData();
        log.info("UpdateUserInfoExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(request.getUser().getUserId() != null && user.getUser().getUserId().equals(request.getUser().getUserId()), "没有授权操作");

        RbacUserEntity updateUser = request.getUser();
        updateUser.setUpdateTime(new Date());
        updateUser.setUserName(null);

        RbacUserEntity userIndb = dao.fetch(RbacUserEntity.class, Cnd.where(RbacUserEntity.FLD_USER_ID, "=", request.getUser().getUserId()));
        assertTrue(userIndb != null, "没有要修改的用户记录");

        //TODO 算法移动到 RBAC服务中
        if (request.getUser().getPassword() != null) {
            if ("LDAP".equals(userIndb.getUserType())) {
                // 需要变更 LDAP数据库中的密码
                return BizResult.error(500, "目前只能通过管理员修改密码,暂不支持自己修改");

            } else {
                try {
                    byte[] decode = Base64.getDecoder().decode(request.getUser().getPassword());
                    String newPwd = new String(decode);
                    if (newPwd.length() >= 6) {
                        updateUser.setPassword(Lang.sha1(RbacConstant.SALT + "_" + newPwd));
                    } else {
                        return BizResult.error(500, "密码最少6位字母或者数字 不填 不更改之前的密码");
                    }
                } catch (Exception e) {
                    return BizResult.error(500, "密码解析错误 不填 不更改之前的密码");
                }
            }
        } else {
            updateUser.setPassword(null);
        }
        updateUser.setStatus(null);
        dao.updateIgnoreNull(updateUser);

        UpdateUserInfoResponse response = new UpdateUserInfoResponse();
        response.setUser(dao.fetch(RbacUserEntity.class, updateUser.getUserId()));

        return BizResult.success(response);
    }
}
