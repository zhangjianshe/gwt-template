package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.SysUserKeyEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryUserKeyRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryUserKeyResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryUserKeyExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryUserKeyExecutor extends AbstractBizExecutor<QueryUserKeyResponse, QueryUserKeyRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<QueryUserKeyResponse> process(BizContext context, BizRequest<QueryUserKeyRequest> bizParam) {
        QueryUserKeyRequest request = bizParam.getData();
        log.info("QueryUserKeyExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        QueryUserKeyResponse response = new QueryUserKeyResponse();
        List<SysUserKeyEntity> query = dao.query(SysUserKeyEntity.class, Cnd.where(SysUserKeyEntity.FLD_USER_ID, "=", user.getUser().getUserId()).asc(SysUserKeyEntity.FLD_CREATE_TIME));
        response.setKeys(query);
        return BizResult.success(response);
    }
}
