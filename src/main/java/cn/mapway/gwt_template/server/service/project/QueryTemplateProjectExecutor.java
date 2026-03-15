package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryTemplateProjectRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryTemplateProjectResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.shared.RbacConstant;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.util.cri.Exps;
import org.nutz.dao.util.cri.SqlExpressionGroup;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryTemplateProjectExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryTemplateProjectExecutor extends AbstractBizExecutor<QueryTemplateProjectResponse, QueryTemplateProjectRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<QueryTemplateProjectResponse> process(BizContext context, BizRequest<QueryTemplateProjectRequest> bizParam) {
        QueryTemplateProjectRequest request = bizParam.getData();
        log.info("QueryTemplateProjectExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        Cnd where = Cnd.where(DevProjectEntity.FLD_IS_TEMPLATE, "=", true);
        if (Strings.isNotBlank(request.getName())) {
            SqlExpressionGroup groupFilter = new SqlExpressionGroup();
            groupFilter.or(Exps.like(DevProjectEntity.FLD_NAME, "%" + request.getName() + "%"));
            groupFilter.or(Exps.like(DevProjectEntity.FLD_TAG, "%" + request.getName() + "%"));
            where.and(groupFilter);
        }
        SqlExpressionGroup group = new SqlExpressionGroup();
        group.or(DevProjectEntity.FLD_USER_ID, "=", RbacConstant.SUPER_USER_ID);
        group.or(DevProjectEntity.FLD_USER_ID, "=", user.getUser().getUserId());
        where.and(group);

        List<DevProjectEntity> projects = dao.query(DevProjectEntity.class, where);
        QueryTemplateProjectResponse response = new QueryTemplateProjectResponse();
        response.setProjects(projects);
        return BizResult.success(response);
    }
}
