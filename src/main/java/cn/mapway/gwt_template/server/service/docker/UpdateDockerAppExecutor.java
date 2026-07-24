package cn.mapway.gwt_template.server.service.docker;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DockerAppEntity;
import cn.mapway.gwt_template.shared.rpc.docker.UpdateDockerAppRequest;
import cn.mapway.gwt_template.shared.rpc.docker.UpdateDockerAppResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * UpdateDockerAppExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateDockerAppExecutor extends AbstractBizExecutor<UpdateDockerAppResponse, UpdateDockerAppRequest> {
    @Resource
    Dao dao;
    @Resource
    DockerAppService dockerAppService;

    @Override
    protected BizResult<UpdateDockerAppResponse> process(BizContext context, BizRequest<UpdateDockerAppRequest> bizParam) {
        UpdateDockerAppRequest request = bizParam.getData();
        log.info("UpdateDockerAppExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        DockerAppEntity appEntity = request.getAppEntity();
        assertNotNull(appEntity, "没有应用信息");
        // 1. RBAC 权限检查
        assertTrue(dockerAppService.canOperate(user),"没有授权操作");

        if (Strings.isBlank(appEntity.getId())) {
            appEntity.setId(R.UU16());
            assertTrue(Strings.isNotBlank(appEntity.getName()), "没有应用名称");
            appEntity.setSummary("");
            dao.insert(appEntity);
        } else {
            dao.updateIgnoreNull(appEntity);
        }

        UpdateDockerAppResponse response = new UpdateDockerAppResponse();
        response.setAppEntity(dao.fetch(DockerAppEntity.class, appEntity.getId()));
        return BizResult.success(response);
    }
}
