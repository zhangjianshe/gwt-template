package cn.mapway.gwt_template.server.service.app;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.AppServiceEntity;
import cn.mapway.gwt_template.shared.rpc.app.UpdateAppServiceRequest;
import cn.mapway.gwt_template.shared.rpc.app.UpdateAppServiceResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;

/**
 * UpdateAppServiceExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateAppServiceExecutor extends AbstractBizExecutor<UpdateAppServiceResponse, UpdateAppServiceRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<UpdateAppServiceResponse> process(BizContext context, BizRequest<UpdateAppServiceRequest> bizParam) {
        UpdateAppServiceRequest request = bizParam.getData();
        log.info("UpdateAppServiceExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(user.isAdmin(), "没有授权操作");
        AppServiceEntity service = request.getService();
        assertNotNull(service, "没有服务数据");

        if (Strings.isNotBlank(service.getId())) {
            if (Strings.isBlank(service.getName())) {
                service.setName(null);
            }
            if (Strings.isBlank(service.getRule())) {
                service.setRule(null);
            }
            dao.updateIgnoreNull(service);
        } else {
            service.setId(R.UU16());
            assertTrue(Strings.isNotBlank(service.getName()), "请输入服务名称");
            assertTrue(Strings.isNotBlank(service.getRule()), "请输入服务规则");
            if (Strings.isBlank(service.getSummary()))
            {
                service.setSummary(service.getName());
            }
            service.setCreateTime(new Timestamp(System.currentTimeMillis()));
            dao.insert(service);
        }
        UpdateAppServiceResponse response = new UpdateAppServiceResponse();
        response.setService(dao.fetch(AppServiceEntity.class, service.getId()));
        return BizResult.success(response);
    }
}
