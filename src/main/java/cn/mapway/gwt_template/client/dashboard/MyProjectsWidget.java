package cn.mapway.gwt_template.client.dashboard;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.desktop.ProjectItem;
import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.workspace.project.ProjectHomePanel;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.rpc.desktop.QueryDesktopRequest;
import cn.mapway.gwt_template.shared.rpc.desktop.QueryDesktopResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.*;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;

import static cn.mapway.gwt_template.client.dashboard.MyProjectsWidget.MODULE_CODE;


@ModuleMarker(
        name = "我的项目",
        value = MODULE_CODE,
        unicode = Fonts.PROJECT,
        summary = "项目列表",
        tags = {AppConstant.TAG_WIDGET}
)
public class MyProjectsWidget extends BaseAbstractModule {
    public final static String MODULE_CODE = "widget_my_projects";
    private static final MyProjectsWidgetUiBinder ourUiBinder = GWT.create(MyProjectsWidgetUiBinder.class);
    private final ClickHandler projectHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            ProjectItem sourceItem = (ProjectItem) event.getSource();
            DevProjectEntity data1 = sourceItem.getData();
            SwitchModuleData switchModuleData = new SwitchModuleData(ProjectHomePanel.MODULE_CODE, "");
            switchModuleData.getParameters().put(data1);
            fireModuleEvent(MyProjectsWidget.this, CommonEvent.switchEvent(switchModuleData));
        }
    };
    @UiField
    HTMLPanel list;

    public MyProjectsWidget() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        boolean b = super.initialize(parentModule, parameter);
        load();
        return b;
    }

    private void load() {
        QueryDesktopRequest request = new QueryDesktopRequest();
        request.setFetchShortcut(false);
        request.setFetchProjects(true);
        request.setFetchWorkspaces(false);
        request.setFetchMainBoard(false);
        AppProxy.get().queryDesktop(request, new AsyncCallback<RpcResult<QueryDesktopResponse>>() {
            @Override
            public void onFailure(Throwable caught) {

                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryDesktopResponse> result) {
                if (result.isSuccess()) {
                    renderItem(result.getData());
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    private void renderItem(QueryDesktopResponse data) {
        list.clear();
        for (DevProjectEntity project : data.getFavoriteProjects()) {
            ProjectItem item = new ProjectItem();
            item.setData(project);
            item.addStyleName(AppResource.INSTANCE.styles().box());
            item.addDomHandler(projectHandler, ClickEvent.getType());
            list.add(item);
        }
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    interface MyProjectsWidgetUiBinder extends UiBinder<HTMLPanel, MyProjectsWidget> {
    }
}