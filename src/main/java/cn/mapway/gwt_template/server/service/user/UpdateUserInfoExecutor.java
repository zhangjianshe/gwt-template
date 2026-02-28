package cn.mapway.gwt_template.server.service.user;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.user.UpdateUserInfoRequest;
import cn.mapway.gwt_template.shared.rpc.user.UpdateUserInfoResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * UpdateUserInfoExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateUserInfoExecutor extends AbstractBizExecutor<UpdateUserInfoResponse, UpdateUserInfoRequest> {
    @Resource
    Dao dao;
    @Override
    protected BizResult<UpdateUserInfoResponse> process(BizContext context, BizRequest<UpdateUserInfoRequest> bizParam) {
        UpdateUserInfoRequest request = bizParam.getData();
        log.info("UpdateUserInfoExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(request.getUser().getUserId()!=null && user.getUser().getUserId().equals(request.getUser().getUserId()),"没有授权操作");

        RbacUserEntity updateUser = request.getUser();
        updateUser.setUpdateTime(new Date());
        updateUser.setUserName(null);
        updateUser.setPassword(null);
        updateUser.setStatus(null);
        dao.updateIgnoreNull(updateUser);

        UpdateUserInfoResponse response = new UpdateUserInfoResponse();
        response.setUser(dao.fetch(RbacUserEntity.class, updateUser.getUserId()));
        
        return BizResult.success(response);
    }
}
