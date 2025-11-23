package cn.mapway.gwt_template.server.service.soft;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.SysSoftwareEntity;
import cn.mapway.gwt_template.shared.rpc.soft.QuerySoftwareRequest;
import cn.mapway.gwt_template.shared.rpc.soft.QuerySoftwareResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * QuerySoftwareExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QuerySoftwareExecutor extends AbstractBizExecutor<QuerySoftwareResponse, QuerySoftwareRequest> {
    @Resource
    Dao dao;
    @Override
    protected BizResult<QuerySoftwareResponse> process(BizContext context, BizRequest<QuerySoftwareRequest> bizParam) {
        QuerySoftwareRequest request = bizParam.getData();
        log.info("QuerySoftwareExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        QuerySoftwareResponse response=new QuerySoftwareResponse();
        response.setSoftwares(dao.query(SysSoftwareEntity.class,Cnd.orderBy().desc(SysSoftwareEntity.FLD_CREATE_TIME)));
        return BizResult.success(response);
    }
}
