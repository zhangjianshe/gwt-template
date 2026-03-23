package cn.mapway.gwt_template.server.service.project.res;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectResourceEntity;
import cn.mapway.gwt_template.shared.db.DevProjectResourceMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.res.QueryProjectResourceRequest;
import cn.mapway.gwt_template.shared.rpc.project.res.QueryProjectResourceResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryProjectResourceExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryProjectResourceExecutor extends AbstractBizExecutor<QueryProjectResourceResponse, QueryProjectResourceRequest> {
    @Resource
    Dao dao;
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<QueryProjectResourceResponse> process(BizContext context, BizRequest<QueryProjectResourceRequest> bizParam) {
        QueryProjectResourceRequest request = bizParam.getData();
        log.info("QueryProjectResourceExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getProjectId()), "没有提供项目ID");
        boolean memberOfProject = projectService.isMemberOfProject(user.getUser().getUserId(), request.getProjectId());
        assertTrue(memberOfProject, "没有授权操作");

        // 修改后的逻辑
        String sqlStr = "SELECT p.*, m." + DevProjectResourceMemberEntity.FLD_PERMISSION +
                " FROM " + DevProjectResourceEntity.TBL_DEV_PROJECT + " as p " +
                " LEFT JOIN " + DevProjectResourceMemberEntity.TBL_DEV_PROJECT + " as m " +
                " ON p." + DevProjectResourceEntity.FLD_ID + " = m." + DevProjectResourceMemberEntity.FLD_RESOURCE_ID +
                " AND m." + DevProjectResourceMemberEntity.FLD_USER_ID + " = @uid " + // 必须限定是当前用户的权限
                " WHERE p." + DevProjectResourceEntity.FLD_PROJECT_ID + " = @pid " +
                " ORDER BY p." + DevProjectResourceEntity.FLD_RANK + " ASC";

        Sql sql = Sqls.create(sqlStr);
        sql.params().set("uid", user.getUser().getUserId()); // 传入当前用户ID
        sql.params().set("pid", request.getProjectId());
        sql.setEntity(dao.getEntity(DevProjectResourceEntity.class));
        sql.setCallback(Sqls.callback.entities()); // 必须设置回调
        dao.execute(sql);

        List<DevProjectResourceEntity> list = sql.getList(DevProjectResourceEntity.class);

        QueryProjectResourceResponse response = new QueryProjectResourceResponse();
        response.setProject(projectService.findProject(request.getProjectId(), user.getUser().getUserId()));
        response.setResources(list);
        return BizResult.success(response);
    }
}
