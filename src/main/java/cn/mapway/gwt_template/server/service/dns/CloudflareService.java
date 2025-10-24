package cn.mapway.gwt_template.server.service.dns;

import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.config.SystemConfigService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.SysConfigEntity;
import cn.mapway.gwt_template.shared.rpc.dns.model.DnsEntry;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CloudflareService {
    @Resource
    SystemConfigService systemConfigService;

    /**
     * 查找我可以管控的DNS列表
     *
     * @return
     */
    public BizResult<List<DnsEntry>> queryDnsList() {
        String token = getToken();
        if (Strings.isBlank(token)) {
            return BizResult.error(500, "没有配置Cloudflare Token");
        }



    }

    private String getToken() {
        BizResult<SysConfigEntity> config = systemConfigService.findConfig(AppConstant.KEY_CLOUDFLARE_TOKEN);
        if (config.isSuccess()) {
            return config.getData().getValue();
        }
        return null;
    }

}
