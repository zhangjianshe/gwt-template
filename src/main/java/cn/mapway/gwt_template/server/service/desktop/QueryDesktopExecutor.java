package cn.mapway.gwt_template.server.service.desktop;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DesktopItemEntity;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.db.DevWorkspaceEntity;
import cn.mapway.gwt_template.shared.rpc.desktop.QueryDesktopRequest;
import cn.mapway.gwt_template.shared.rpc.desktop.QueryDesktopResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryDesktopExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryDesktopExecutor extends AbstractBizExecutor<QueryDesktopResponse, QueryDesktopRequest> {
    @Resource
    Dao dao;
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<QueryDesktopResponse> process(BizContext context, BizRequest<QueryDesktopRequest> bizParam) {
        QueryDesktopRequest request = bizParam.getData();
        log.info("QueryDesktopExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        List<DesktopItemEntity> result = dao.query(DesktopItemEntity.class, Cnd.where(DesktopItemEntity.FLD_USER_ID, "=", user.getUser().getUserId())
                .or(DesktopItemEntity.FLD_SHARE, "=", true).asc(DesktopItemEntity.FLD_RANK));

        QueryDesktopResponse response = new QueryDesktopResponse();

        List<DevWorkspaceEntity> workspaces = projectService.queryMyWorkspaces(user.getUser().getUserId());
        projectService.fillWorkspaceInfo(workspaces, false);
        response.setItems(result);
        response.setWorkspaces(workspaces);
        List<DevProjectEntity> projectEntities = projectService.queryFavoriteProjects(user.getUser().getUserId());
        for (DevProjectEntity projectEntity : projectEntities) {
            projectService.fillProjectExtraInformation(projectEntity, user.getUser().getUserId());
        }

        response.setFavoriteProjects(projectEntities);
        return BizResult.success(response);
    }
}
