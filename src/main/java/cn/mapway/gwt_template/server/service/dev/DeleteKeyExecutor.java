package cn.mapway.gwt_template.server.service.dev;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevKeyEntity;
import cn.mapway.gwt_template.shared.rpc.dev.DeleteKeyRequest;
import cn.mapway.gwt_template.shared.rpc.dev.DeleteKeyResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * DeleteKeyExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteKeyExecutor extends AbstractBizExecutor<DeleteKeyResponse, DeleteKeyRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<DeleteKeyResponse> process(BizContext context, BizRequest<DeleteKeyRequest> bizParam) {
        DeleteKeyRequest request = bizParam.getData();
        log.info("DeleteKeyExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertNotNull(request.getKeyId(), "请输入密钥ID");
        dao.delete(DevKeyEntity.class, request.getKeyId());
        return BizResult.success(new DeleteKeyResponse());
    }
}
