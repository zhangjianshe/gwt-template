package cn.mapway.gwt_template.server.service.desktop;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DashboardEntity;
import cn.mapway.gwt_template.shared.db.DesktopItemEntity;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.db.DevWorkspaceEntity;
import cn.mapway.gwt_template.shared.rpc.desktop.DashboardItemData;
import cn.mapway.gwt_template.shared.rpc.desktop.QueryDesktopRequest;
import cn.mapway.gwt_template.shared.rpc.desktop.QueryDesktopResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import cn.mapway.ui.client.fonts.Fonts;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.ArrayList;
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

        QueryDesktopResponse response = new QueryDesktopResponse();

        if (request.isFetchShortcut()) {
            List<DesktopItemEntity> result = dao.query(DesktopItemEntity.class, Cnd.where(DesktopItemEntity.FLD_USER_ID, "=", user.getUser().getUserId())
                    .or(DesktopItemEntity.FLD_SHARE, "=", true).asc(DesktopItemEntity.FLD_RANK));
            response.setItems(result);
        }


        if (request.isFetchWorkspaces()) {
            List<DevWorkspaceEntity> workspaces = projectService.queryMyWorkspaces(user.getUser().getUserId());
            projectService.fillWorkspaceInfo(workspaces, false);
            response.setWorkspaces(workspaces);
        }
        if (request.isFetchProjects()) {
            List<DevProjectEntity> projectEntities = projectService.queryMyProjects(user.getUser().getUserId());
            log.info("user {} has {} projects", user.getUser().getUserId(), projectEntities.size());
            for (DevProjectEntity projectEntity : projectEntities) {
                projectService.fillProjectExtraInformation(projectEntity, user.getUser().getUserId());
            }
            response.setFavoriteProjects(projectEntities);
        }

        if (request.isFetchMainBoard()) {
            String boardName = "MAIN_BOARD";
            Cnd where = Cnd.where(DashboardEntity.FLD_NAME, "=", boardName);
            where.and(DashboardEntity.FLD_USER_ID, "=", user.getUser().getUserId());
            DashboardEntity layout = dao.fetch(DashboardEntity.class, where);
            if (layout == null) {
                //首次访问 需要创建一个缺省的面板
                layout = createOne(user.getUser(), boardName);
            }
            response.setDashboard(layout);
        }
        return BizResult.success(response);
    }

    private DashboardEntity createOne(RbacUserEntity user, String boardName) {
        DashboardEntity dashboard = new DashboardEntity();
        dashboard.setId(R.UU16());
        dashboard.setName(boardName);
        dashboard.setUserId(user.getUserId());
        dashboard.setCreateTime(new Timestamp(System.currentTimeMillis()));
        dashboard.setIcon(Fonts.LAYOUT);
        dashboard.setSummary("系统创建缺省面板");

        List<DashboardItemData> layouts = new ArrayList<>();
        layouts.add(create("widget_shortcut", 2, 1, 3, 3, ""));
        String defaultLayout = "[{\"moduleCode\":\"widget_my_projects\",\"x\":0,\"y\":0,\"w\":4,\"h\":11},{\"moduleCode\":\"widget_shortcut\",\"x\":4,\"y\":0,\"w\":8,\"h\":3}]";
        dashboard.setLayout(defaultLayout);
        dao.insert(dashboard);
        return dashboard;
    }

    private DashboardItemData create(String moduleCode, int x, int y, int w, int h, String param) {
        DashboardItemData data = new DashboardItemData();
        data.id = "G" + R.captchaChar(7);
        data.moduleCode = moduleCode;
        data.x = (double) x;
        data.y = (double) y;
        data.w = (double) w;
        data.h = (double) h;
        data.parameter = param;
        return data;
    }
}
