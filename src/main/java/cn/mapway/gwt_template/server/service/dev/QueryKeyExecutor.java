package cn.mapway.gwt_template.server.service.dev;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevKeyEntity;
import cn.mapway.gwt_template.shared.rpc.dev.QueryKeyRequest;
import cn.mapway.gwt_template.shared.rpc.dev.QueryKeyResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.FieldMatcher;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryKeyExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryKeyExecutor extends AbstractBizExecutor<QueryKeyResponse, QueryKeyRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<QueryKeyResponse> process(BizContext context, BizRequest<QueryKeyRequest> bizParam) {
        QueryKeyRequest request = bizParam.getData();
        log.info("QueryKeyExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        List<DevKeyEntity> query = dao.query(DevKeyEntity.class, Cnd.orderBy().desc(DevKeyEntity.FLD_CREATE_TIME),null,
                FieldMatcher.make(null,DevKeyEntity.FLD_PRIVATE_KEY,false)
        );
        QueryKeyResponse response = new QueryKeyResponse();
        response.setKeys(query);
        return BizResult.success(response);
    }
}
