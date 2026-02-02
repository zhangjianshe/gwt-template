package cn.mapway.gwt_template.server.service.app;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.config.SystemConfigService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.SysConfigEntity;
import cn.mapway.gwt_template.shared.rpc.app.UpdateAppInfoRequest;
import cn.mapway.gwt_template.shared.rpc.app.UpdateAppInfoResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;

/**
 * UpdateAppInfoExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateAppInfoExecutor extends AbstractBizExecutor<UpdateAppInfoResponse, UpdateAppInfoRequest> {
    @Resource
    SystemConfigService systemConfigService;

    @Override
    protected BizResult<UpdateAppInfoResponse> process(BizContext context, BizRequest<UpdateAppInfoRequest> bizParam) {
        UpdateAppInfoRequest request = bizParam.getData();
        log.info("UpdateAppInfoExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(user.isAdmin(), "没有授权操作");
        assertTrue(request.getAppData() != null, "没有数据可以更新");

        SysConfigEntity config = new SysConfigEntity();
        config.setKey(AppConstant.KEY_APPLICATION_INFO);
        config.setValue(Json.toJson(request.getAppData()));
        config.setCreateTime(new Timestamp(System.currentTimeMillis()));
        systemConfigService.saveOrUpdate(config);
        return BizResult.success(new UpdateAppInfoResponse());
    }
}
