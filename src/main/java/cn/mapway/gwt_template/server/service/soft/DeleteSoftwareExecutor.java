package cn.mapway.gwt_template.server.service.soft;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.SysSoftwareEntity;
import cn.mapway.gwt_template.shared.db.SysSoftwareFileEntity;
import cn.mapway.gwt_template.shared.rpc.soft.DeleteSoftwareRequest;
import cn.mapway.gwt_template.shared.rpc.soft.DeleteSoftwareResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * DeleteSoftwareExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteSoftwareExecutor extends AbstractBizExecutor<DeleteSoftwareResponse, DeleteSoftwareRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<DeleteSoftwareResponse> process(BizContext context, BizRequest<DeleteSoftwareRequest> bizParam) {
        DeleteSoftwareRequest request = bizParam.getData();
        log.info("DeleteSoftwareExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertNotNull(request.getSoftwareId(), "没有软件ID");

        dao.clear(SysSoftwareFileEntity.class, Cnd.where(SysSoftwareFileEntity.FLD_SOFTWARE_ID, "=", request.getSoftwareId()));
        dao.delete(SysSoftwareEntity.class, request.getSoftwareId());

        return BizResult.success(new DeleteSoftwareResponse());
    }
}
