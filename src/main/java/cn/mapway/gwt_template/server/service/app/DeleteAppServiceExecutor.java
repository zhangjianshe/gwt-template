package cn.mapway.gwt_template.server.service.app;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.AppServiceEntity;
import cn.mapway.gwt_template.shared.rpc.app.DeleteAppServiceRequest;
import cn.mapway.gwt_template.shared.rpc.app.DeleteAppServiceResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * DeleteAppServiceExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteAppServiceExecutor extends AbstractBizExecutor<DeleteAppServiceResponse, DeleteAppServiceRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<DeleteAppServiceResponse> process(BizContext context, BizRequest<DeleteAppServiceRequest> bizParam) {
        DeleteAppServiceRequest request = bizParam.getData();
        log.info("DeleteAppServiceExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getServiceId()), "输入数据");
        assertTrue(user.isAdmin(), "没有授权操作");
        dao.delete(AppServiceEntity.class, request.getServiceId());
        return BizResult.success(new DeleteAppServiceResponse());
    }
}
