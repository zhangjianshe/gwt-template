package cn.mapway.gwt_template.client.workspace;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.resource.AppCss;
import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.user.UserIcon;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.db.DevWorkspaceFolderEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevProjectRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevProjectResponse;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.Header;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HTMLTable;

import java.util.HashMap;
import java.util.Map;

public class WorkspaceFolder extends CommonEventComposite implements IData<DevWorkspaceFolderEntity> {

    private static final WorkspaceFolderUiBinder ourUiBinder = GWT.create(WorkspaceFolderUiBinder.class);
    @UiField
    Header lbFolderName;
    @UiField
    Anchor btnCreateProject;
    @UiField
    FlexTable table;
    Map<String, DevProjectEntity> projects = new HashMap<>();
    private DevWorkspaceFolderEntity folder;

    public WorkspaceFolder() {
        initWidget(ourUiBinder.createAndBindUi(this));
        table.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                HTMLTable.Cell cell = table.getCellForEvent(event);
                if (cell != null && cell.getRowIndex() > 0) { // 排除表头
                    int row = cell.getRowIndex();
                    String projectId = table.getRowFormatter().getElement(row).getAttribute("alt");
                    DevProjectEntity project = projects.get(projectId);
                    if (project != null) {
                        fireEvent(CommonEvent.selectEvent(project));
                    }
                }
            }
        });
    }

    @Override
    public DevWorkspaceFolderEntity getData() {
        return folder;
    }

    @Override
    public void setData(DevWorkspaceFolderEntity obj) {
        folder = obj;
        toUI();
    }

    private void toUI() {
        lbFolderName.setText(folder.getName());
        table.removeAllRows(); // 使用 removeAllRows 比 clear 更彻底
        projects.clear();
        AppCss appCss = AppResource.INSTANCE.styles();
        // 设置表头样式
        table.addStyleName(appCss.table());
        table.getRowFormatter().addStyleName(0, appCss.tableHeader());

        int col = 0;
        table.setText(0, col++, "项目名称");
        table.setText(0, col++, "创建时间");
        table.setText(0, col++, "项目进度");
        table.setText(0, col++, "成员数量");
        table.setText(0, col++, "创建人");
    }

    public void addProject(DevProjectEntity project) {
        int row = table.getRowCount();
        int col = 0;
        AppCss css = AppResource.INSTANCE.styles();

        // 1. 项目名称
        table.setText(row, col++, project.getName());

        // 2. 创建时间
        table.setText(row, col++, StringUtil.formatDate(project.getCreateTime(), "yyyy-MM-dd"));

        // 3. 项目进度
        table.setText(row, col++, project.getProgress());
        if (project.getProgress() != null && project.getProgress().contains("100")) {
            table.getCellFormatter().addStyleName(row, col - 1, css.success());
        }

        // 4. 成员数量
        table.setText(row, col++, project.getMemberCount() + " 人");

        // 5. 创建人（包含头像）
        UserIcon userIcon = new UserIcon();
        userIcon.setUserInformation(project.getUserId(), project.getCreateUserName(), project.getCreateUserAvatar()).setImageSize(24, 24);
        table.setWidget(row, col++, userIcon);

        // 行样式美化
        HTMLTable.RowFormatter rowFormatter = table.getRowFormatter();
        rowFormatter.addStyleName(row, css.tableRow());
        rowFormatter.getElement(row).setAttribute("alt", project.getId());
        projects.put(project.getId(), project);

    }

    @UiHandler("btnCreateProject")
    public void btnCreateProjectClick(ClickEvent event) {
        ClientContext.get().input("创建项目", "项目名称", "", "", new Callback() {
            @Override
            public void onFailure(Object reason) {

            }

            @Override
            public void onSuccess(Object result) {
                String projectName = (String) result;
                if (!StringUtil.isBlank(projectName)) {
                    doCreateProject(projectName);
                }
            }

        });
    }

    private void doCreateProject(String projectName) {
        UpdateDevProjectRequest request = new UpdateDevProjectRequest();
        DevProjectEntity project = new DevProjectEntity();
        project.setFolderId(folder.getId());
        project.setWorkspaceId(folder.getWorkspaceId());
        project.setName(projectName);
        request.setProject(project);
        AppProxy.get().updateDevProject(request, new AsyncCallback<RpcResult<UpdateDevProjectResponse>>() {
            @Override
            public void onFailure(Throwable caught) {

            }

            @Override
            public void onSuccess(RpcResult<UpdateDevProjectResponse> result) {
                if (result.isSuccess()) {
                    fireEvent(CommonEvent.reloadEvent(null));
                }
            }
        });
    }

    interface WorkspaceFolderUiBinder extends UiBinder<HTMLPanel, WorkspaceFolder> {
    }
}