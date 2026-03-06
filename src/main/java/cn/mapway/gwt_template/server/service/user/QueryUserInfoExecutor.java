package cn.mapway.gwt_template.server.service.user;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.user.QueryUserInfoRequest;
import cn.mapway.gwt_template.shared.rpc.user.QueryUserInfoResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.util.cri.Exps;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * QueryUserInfoExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryUserInfoExecutor extends AbstractBizExecutor<QueryUserInfoResponse, QueryUserInfoRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<QueryUserInfoResponse> process(BizContext context, BizRequest<QueryUserInfoRequest> bizParam) {
        QueryUserInfoRequest request = bizParam.getData();
        log.info("QueryUserInfoExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        if (request.getUserIdList() == null) {
            request.setUserIdList(new ArrayList<>());
        }
        Cnd where = Cnd.where(Exps.inLong(RbacUserEntity.FLD_USER_ID, request.getUserIdList().toArray(new Long[0])));
        List<RbacUserEntity> query = dao.query(RbacUserEntity.class, where);
        for (RbacUserEntity userEntity : query) {
            userEntity.setPassword("*******");
        }
        QueryUserInfoResponse response = new QueryUserInfoResponse();
        response.setUsers(query);
        return BizResult.success(response);
    }
}
