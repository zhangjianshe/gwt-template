package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectTeamMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateFavoriteProjectRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateFavoriteProjectResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * UpdateFavoriteProjectExecutor
 * 将我参与的项目 设定为 Favorite Project
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateFavoriteProjectExecutor extends AbstractBizExecutor<UpdateFavoriteProjectResponse, UpdateFavoriteProjectRequest> {
    @Resource
    ProjectService projectService;
    @Resource
    Dao dao;

    @Override
    protected BizResult<UpdateFavoriteProjectResponse> process(BizContext context, BizRequest<UpdateFavoriteProjectRequest> bizParam) {
        UpdateFavoriteProjectRequest request = bizParam.getData();
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        // 1. Validation
        if (request == null || Strings.isBlank(request.getProjectId())) {
            return BizResult.error(400, "没有项目ID");
        }

        // 2. Permission Check
        Long userId = user.getUser().getUserId();
        boolean memberOfProject = projectService.isMemberOfProject(userId, request.getProjectId());
        if (!memberOfProject) {
            return BizResult.error(403, "不是项目成员，无法操作");
        }

        // 3. Secure Update using Parameterized SQL
        // Using @ notation for Nutz Sql placeholders
        String sqlStr = "UPDATE " + DevProjectTeamMemberEntity.TBL_DEV_PROJECT_TEAM_MEMBER +
                " SET " + DevProjectTeamMemberEntity.FLD_FAVORITE + " = @favorite" +
                " WHERE " + DevProjectTeamMemberEntity.FLD_PROJECT_ID + " = @projectId" +
                " AND " + DevProjectTeamMemberEntity.FLD_USER_ID + " = @userId";

        Sql sql = Sqls.create(sqlStr);
        sql.params().set("favorite", request.getFavorite());
        sql.params().set("projectId", request.getProjectId());
        sql.params().set("userId", userId);

        dao.execute(sql);

        return BizResult.success(new UpdateFavoriteProjectResponse());
    }
}
