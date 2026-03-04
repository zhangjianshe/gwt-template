package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevWorkspaceMemberRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevWorkspaceMemberResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.DevWorkspaceMember;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryDevWorkspaceMemberExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryDevWorkspaceMemberExecutor extends AbstractBizExecutor<QueryDevWorkspaceMemberResponse, QueryDevWorkspaceMemberRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<QueryDevWorkspaceMemberResponse> process(BizContext context, BizRequest<QueryDevWorkspaceMemberRequest> bizParam) {
        QueryDevWorkspaceMemberRequest request = bizParam.getData();
        String workspaceId = request.getWorkspaceId();
        assertTrue(Strings.isNotBlank(workspaceId), "必须指定工作空间ID");

        // T1: 成员权限表, T2: RBAC用户表
        String sqlStr = "SELECT " +
                "T2.user_id as userId, T2.user_name as userName, T2.nick_name as nickName, " +
                "T2.avatar, T2.email, " +
                "T1.workspace_id as workspaceId, T1.create_time as createTime, " +
                "T1.is_owner as isOwner, T1.permission " +
                "FROM dev_workspace_member T1 " +
                "INNER JOIN rbac_user T2 ON T1.user_id = T2.user_id " +
                "WHERE T1.workspace_id = @wid " +
                "ORDER BY T1.is_owner DESC, T1.create_time ASC";

        Sql sql = Sqls.create(sqlStr);
        sql.setParam("wid", workspaceId);

        // 映射到我们新创建的 DevWorkspaceMember 类
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao.getEntity(DevWorkspaceMember.class));

        dao.execute(sql);
        List<DevWorkspaceMember> members = sql.getList(DevWorkspaceMember.class);

        QueryDevWorkspaceMemberResponse response = new QueryDevWorkspaceMemberResponse();
        // 确保你的 Response 类中的 List 类型已经改为 List<DevWorkspaceMember>
        response.setMembers(members);

        return BizResult.success(response);
    }
}
