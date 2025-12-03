package cn.mapway.gwt_template.server.service.dev;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.config.CertService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevKeyEntity;
import cn.mapway.gwt_template.shared.rpc.dev.CreateKeyRequest;
import cn.mapway.gwt_template.shared.rpc.dev.CreateKeyResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;

/**
 * CreateKeyExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class CreateKeyExecutor extends AbstractBizExecutor<CreateKeyResponse, CreateKeyRequest> {
    @Resource
    Dao dao;
    @Resource
    CertService certService;

    @Override
    protected BizResult<CreateKeyResponse> process(BizContext context, BizRequest<CreateKeyRequest> bizParam) {
        CreateKeyRequest request = bizParam.getData();
        log.info("CreateKeyExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getName()), "没有设定名称");
        //TODO 关联用户
        DevKeyEntity fetch = dao.fetch(DevKeyEntity.class, Cnd.where(DevKeyEntity.FLD_NAME, "=", request.getName()));
        if (fetch != null) {
            return BizResult.error(500, "名称已被使用");
        }

        String[] keyPairs = CertService.genOpenSshKey("soft");

        DevKeyEntity devKeyEntity = new DevKeyEntity();
        devKeyEntity.setId(R.UU16());
        devKeyEntity.setName(request.getName());
        devKeyEntity.setPrivateKey(keyPairs[0]);
        devKeyEntity.setPublicKey(keyPairs[1]);
        devKeyEntity.setUserId(0L);
        devKeyEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
        dao.insert(devKeyEntity);
        return BizResult.success(new CreateKeyResponse());
    }
}
