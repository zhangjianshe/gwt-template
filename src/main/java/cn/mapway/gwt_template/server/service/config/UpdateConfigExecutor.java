package cn.mapway.gwt_template.server.service.config;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.user.login.LoginProvider;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.SysConfigEntity;
import cn.mapway.gwt_template.shared.rpc.config.ConfigEnums;
import cn.mapway.gwt_template.shared.rpc.config.UpdateConfigRequest;
import cn.mapway.gwt_template.shared.rpc.config.UpdateConfigResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;

/**
 * UpdateConfigExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateConfigExecutor extends AbstractBizExecutor<UpdateConfigResponse, UpdateConfigRequest> {
    @Resource
    SystemConfigService systemConfigService;
    @Resource
    LoginProvider loginProvider;

    @Override
    protected BizResult<UpdateConfigResponse> process(BizContext context, BizRequest<UpdateConfigRequest> bizParam) {
        UpdateConfigRequest request = bizParam.getData();
        log.info("UpdateConfigExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(user.isAdmin(), "没有授权操作");
        if (request.getAppData() != null) {
            saveOrUpdate(ConfigEnums.CONFIG_APP.getCode(), request.getAppData());
        }
        if (request.getLdapSettings() != null) {
            saveOrUpdate(ConfigEnums.CONFIG_LDAP.getCode(), request.getLdapSettings());
            loginProvider.reset();
        }
        return BizResult.success(new UpdateConfigResponse());
    }

    private void saveOrUpdate(String name, Object object) {
        SysConfigEntity temp = new SysConfigEntity();
        temp.setCreateTime(new Timestamp(System.currentTimeMillis()));
        temp.setKey(name);
        temp.setValue(Json.toJson(object));
        systemConfigService.saveOrUpdate(temp);
    }
}
