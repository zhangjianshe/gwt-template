package cn.mapway.gwt_template.server.service.soft;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.SysSoftwareFileEntity;
import cn.mapway.gwt_template.shared.rpc.soft.QuerySoftwareFilesRequest;
import cn.mapway.gwt_template.shared.rpc.soft.QuerySoftwareFilesResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QuerySoftwareFilesExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QuerySoftwareFilesExecutor extends AbstractBizExecutor<QuerySoftwareFilesResponse, QuerySoftwareFilesRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<QuerySoftwareFilesResponse> process(BizContext context, BizRequest<QuerySoftwareFilesRequest> bizParam) {
        QuerySoftwareFilesRequest request = bizParam.getData();
        log.info("QuerySoftwareFilesExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getSoftwareId()), "没有软件ID");

        Cnd where = Cnd.where(SysSoftwareFileEntity.FLD_SOFTWARE_ID, "=", request.getSoftwareId());
        where.desc(SysSoftwareFileEntity.FLD_VERSION).asc(SysSoftwareFileEntity.FLD_NAME);
        List<SysSoftwareFileEntity> files = dao.query(SysSoftwareFileEntity.class, where);
        QuerySoftwareFilesResponse response = new QuerySoftwareFilesResponse();
        response.setFiles(files);
        return BizResult.success(response);
    }
}
