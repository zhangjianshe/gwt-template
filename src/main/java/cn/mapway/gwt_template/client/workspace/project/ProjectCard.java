package cn.mapway.gwt_template.client.workspace.project;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.rpc.file.SecurityLevel;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevProjectRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevProjectResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.ui.client.mvc.IToolsProvider;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

public class ProjectCard extends CommonEventComposite implements IToolsProvider, IData<String> {
    private static final ProjectCardUiBinder ourUiBinder = GWT.create(ProjectCardUiBinder.class);
    @UiField
    HTMLPanel pnlName;
    @UiField
    HTMLPanel pnlSummary;
    @UiField
    com.google.gwt.dom.client.Element lbCreator;
    @UiField
    com.google.gwt.dom.client.Element lbMemberCount;
    @UiField
    com.google.gwt.dom.client.Element lbProgress;
    @UiField
    InlineLabel lbCreateTime;
    @UiField
    Label lbSecurity;
    @UiField
    AiButton btnEdit;
    @UiField
    HorizontalPanel tools;
    @UiField
    HTMLPanel card;
    DevProjectEntity project;
    private String projectId;

    public ProjectCard() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public String getData() {
        return projectId;
    }

    @Override
    public void setData(String obj) {
        projectId = obj;
        loadProject(projectId);
    }

    private void loadProject(String projectId) {
        QueryDevProjectRequest request = new QueryDevProjectRequest();
        request.setProjectId(projectId);
        AppProxy.get().queryDevProject(request, new AsyncCallback<RpcResult<QueryDevProjectResponse>>() {
            @Override
            public void onFailure(Throwable caught) {

            }

            @Override
            public void onSuccess(RpcResult<QueryDevProjectResponse> result) {
                if (result.isSuccess()) {

                    renderProject(result.getData().getProjects().get(0));
                }
            }
        });
    }

    private void renderProject(DevProjectEntity project) {
        this.project = project;
        //秘密等级
        SecurityLevel securityLevel = SecurityLevel.fromRank(project.getSecurityLevel());

        lbSecurity.setText(securityLevel.getText());
        lbSecurity.getElement().getStyle().setColor(securityLevel.getColor());

        // 1. 基础文字设置
        pnlName.getElement().setInnerText(project.getName());
        pnlSummary.getElement().setInnerText(project.getSummary());
        lbCreator.setInnerText(project.getCreateUserName() != null ? project.getCreateUserName() : "未知");
        lbMemberCount.setInnerText(String.valueOf(project.getMemberCount() != null ? project.getMemberCount() : 0));
        lbProgress.setInnerText(project.getProgress() != null ? project.getProgress() : "0%");

        // 2. 处理时间 (Timestamp -> String)
        if (project.getCreateTime() != null) {
            lbCreateTime.setText(DateTimeFormat.getFormat("yyyy-MM-dd").format(project.getCreateTime()));
        }

        // 3. 应用项目主颜色 (Color)
        String themeColor = (project.getColor() != null && !project.getColor().isEmpty()) ? project.getColor() : "#4a90e2";
        CommonPermission commonPermission = CommonPermission.from(project.getCurrentUserPermission());
        if(StringUtil.isNotBlank(project.getIcon()))
        {
            Style style = card.getElement().getStyle();
            style.setBackgroundImage("url('" + project.getIcon() + "')");
        }
        btnEdit.setVisible(commonPermission.isSuper());
    }

    @UiHandler("btnEdit")
    public void btnEditClick(ClickEvent event) {
        Dialog<DevProjectEditor> dialog = DevProjectEditor.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isUpdate()) {
                    DevProjectEntity entity = event.getValue();
                    entity.setCurrentUserPermission(project.getCurrentUserPermission());
                    renderProject(entity);
                    dialog.hide();
                } else if (event.isClose()) {
                    dialog.hide();
                }
            }
        });
        dialog.getContent().setData(project);
        dialog.center();

    }

    @Override
    public Widget getTools() {
        return tools;
    }

    interface ProjectCardUiBinder extends UiBinder<HTMLPanel, ProjectCard> {
    }
}