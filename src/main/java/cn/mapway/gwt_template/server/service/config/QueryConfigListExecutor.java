package cn.mapway.gwt_template.server.service.config;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.SysConfigEntity;
import cn.mapway.gwt_template.shared.rpc.config.QueryConfigListRequest;
import cn.mapway.gwt_template.shared.rpc.config.QueryConfigListResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
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
 * QueryConfigListExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryConfigListExecutor extends AbstractBizExecutor<QueryConfigListResponse, QueryConfigListRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<QueryConfigListResponse> process(BizContext context, BizRequest<QueryConfigListRequest> bizParam) {
        QueryConfigListRequest request = bizParam.getData();
        log.info("QueryConfigListExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        //TODO only administrator can do this
        QueryConfigListResponse response = new QueryConfigListResponse();
        if (request.getKeys() == null || request.getKeys().isEmpty()) {
            response.setConfigs(new ArrayList<>());
        } else {
            String[] keys = request.getKeys().toArray(new String[0]);
            Cnd where = Cnd.where(Exps.inStr(SysConfigEntity.FLD_KEY, keys));
            List<SysConfigEntity> result = dao.query(SysConfigEntity.class, where);
            response.setConfigs(result);
        }
        return BizResult.success(response);
    }
}
