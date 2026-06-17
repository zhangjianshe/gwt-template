package cn.mapway.gwt_template.server.service.powerdns;

import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.config.SystemConfigService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.powerdns.PowerDnsConfig;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class PowerDnsService {
    private PowerDnsConfig powerDnsConfig = null;
    @Resource
    SystemConfigService systemConfigService;

    public synchronized BizResult<PowerDnsConfig> getPowerDnsConfig() {
        if (powerDnsConfig == null) {
            PowerDnsConfig powerDnsConfig1 = systemConfigService.getConfigFromKeyAsObject(AppConstant.KEY_POWER_DNS, PowerDnsConfig.class);

            if (powerDnsConfig1 != null && Strings.isNotBlank(powerDnsConfig1.basePath)) {
                powerDnsConfig = powerDnsConfig1;
            } else {
                return BizResult.error(500, "没有配置PowerDNS");
            }
        }
        return BizResult.success(powerDnsConfig);
    }

    public void reset() {
        powerDnsConfig = null;
    }


}
