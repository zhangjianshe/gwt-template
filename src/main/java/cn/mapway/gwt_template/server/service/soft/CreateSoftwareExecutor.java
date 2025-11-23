package cn.mapway.gwt_template.server.service.soft;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.SysSoftwareEntity;
import cn.mapway.gwt_template.shared.rpc.soft.CreateSoftwareRequest;
import cn.mapway.gwt_template.shared.rpc.soft.CreateSoftwareResponse;
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
 * CreateSoftwareExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class CreateSoftwareExecutor extends AbstractBizExecutor<CreateSoftwareResponse, CreateSoftwareRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<CreateSoftwareResponse> process(BizContext context, BizRequest<CreateSoftwareRequest> bizParam) {
        CreateSoftwareRequest request = bizParam.getData();
        log.info("CreateSoftwareExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        SysSoftwareEntity software = request.getSoftware();
        assertNotNull(request.getSoftware(), "没有软件需要更更新");
        if (Strings.isBlank(software.getId())) {
            assertTrue(Strings.isNotBlank(software.getName()), "名称必须填写");
            software.setId(R.UU16());
            software.setCreateTime(new Timestamp(System.currentTimeMillis()));
            software.setToken(R.UU16());
            if (Strings.isBlank(software.getLogo())) {
                software.setLogo("img/software.png");
            }
            if (Strings.isBlank(software.getSummary())) {
                software.setSummary(software.getName());
            }
            dao.insert(software);
        } else {
            software.setToken(null);
            dao.updateIgnoreNull(software);
        }
        CreateSoftwareResponse response = new CreateSoftwareResponse();
        response.setSoftware(dao.fetch(SysSoftwareEntity.class, software.getId()));
        return BizResult.success(response);
    }
}
