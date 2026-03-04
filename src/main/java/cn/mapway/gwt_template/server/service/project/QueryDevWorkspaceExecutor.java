package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevWorkspaceEntity;
import cn.mapway.gwt_template.shared.db.DevWorkspaceMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevWorkspaceRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevWorkspaceResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryDevWorkspaceExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryDevWorkspaceExecutor extends AbstractBizExecutor<QueryDevWorkspaceResponse, QueryDevWorkspaceRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<QueryDevWorkspaceResponse> process(BizContext context, BizRequest<QueryDevWorkspaceRequest> bizParam) {
        QueryDevWorkspaceRequest request = bizParam.getData();
        log.info("QueryDevWorkspaceExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        Long userId = user.getUser().getUserId();

        // 逻辑：从成员表找到对应的 workspace_id 集合，然后查询空间详情
        // 使用子查询方式： WHERE id IN (SELECT workspace_id FROM dev_workspace_member WHERE user_id = ?)
        Cnd cnd = Cnd.where(DevWorkspaceEntity.FLD_ID, "IN",
                Sqls.create("SELECT " + DevWorkspaceMemberEntity.FLD_WORKSPACE_ID +
                                " FROM " + DevWorkspaceMemberEntity.TBL_DEV_WORKSPACE_MEMBER +
                                " WHERE " + DevWorkspaceMemberEntity.FLD_USER_ID + " = @uid")
                        .setParam("uid", userId));

        // 按创建时间倒序排列，新创建的在前面
        cnd.desc(DevWorkspaceEntity.FLD_CREATE_TIME);

        List<DevWorkspaceEntity> workspaces = dao.query(DevWorkspaceEntity.class, cnd);

        QueryDevWorkspaceResponse response = new QueryDevWorkspaceResponse();
        response.setWorkspaces(workspaces);

        return BizResult.success(response);
    }
}
