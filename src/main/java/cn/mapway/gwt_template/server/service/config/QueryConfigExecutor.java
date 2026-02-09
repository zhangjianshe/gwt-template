package cn.mapway.gwt_template.server.service.config;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.config.AppConfig;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.app.AppData;
import cn.mapway.gwt_template.shared.rpc.config.ConfigEnums;
import cn.mapway.gwt_template.shared.rpc.config.QueryConfigRequest;
import cn.mapway.gwt_template.shared.rpc.config.QueryConfigResponse;
import cn.mapway.gwt_template.shared.rpc.user.ldap.LdapSettings;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * QueryConfigExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryConfigExecutor extends AbstractBizExecutor<QueryConfigResponse, QueryConfigRequest> {
    private final SystemConfigService systemConfigService;
    @Resource
    AppConfig appConfig;

    public QueryConfigExecutor(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @Override
    protected BizResult<QueryConfigResponse> process(BizContext context, BizRequest<QueryConfigRequest> bizParam) {
        QueryConfigRequest request = bizParam.getData();
        log.info("QueryConfigExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(request.getConfigKeys() != null, "没有请求列表");
        QueryConfigResponse response = new QueryConfigResponse();
        for (String key : request.getConfigKeys()) {
            if (Strings.isBlank(key)) {
                continue;
            }
            ConfigEnums configEnums = ConfigEnums.fromCode(key);
            switch (configEnums) {
                case CONFIG_APP:
                    AppData appData = systemConfigService.getConfigFromKeyAsObject(key, AppData.class);
                    response.setAppData(Objects.requireNonNullElseGet(appData, AppData::new));
                    appData.setSshPort(appConfig.getSshPort());
                    break;
                case CONFIG_LDAP:
                    LdapSettings ldapSettings = systemConfigService.getConfigFromKeyAsObject(key, LdapSettings.class);
                    response.setLdapSettings(Objects.requireNonNullElseGet(ldapSettings, LdapSettings::new));
                    break;
                default:
                    return BizResult.error(500, "不支持配置" + key);
            }
        }
        return BizResult.success(response);
    }

}
