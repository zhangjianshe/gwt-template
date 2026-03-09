package cn.mapway.gwt_template.client.workspace.project;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevProjectRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevProjectResponse;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;

public class ProjectCard extends CommonEventComposite implements IData<String> {
    private static final ProjectCardUiBinder ourUiBinder = GWT.create(ProjectCardUiBinder.class);
    @UiField
    HTMLPanel pnlColorBar;
    @UiField
    HTMLPanel pnlIconBox;
    @UiField
    com.google.gwt.dom.client.Element iconElement;
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
        // 1. 基础文字设置
        pnlName.getElement().setInnerText(project.getName());
        pnlSummary.getElement().setInnerText(project.getSummary());
        lbCreator.setInnerText(project.getCreateUserName() != null ? project.getCreateUserName() : "未知");
        lbMemberCount.setInnerText(String.valueOf(project.getMemberCount() != null ? project.getMemberCount() : 0));
        lbProgress.setInnerText(project.getProgress() != null ? project.getProgress() + "%" : "0%");

        // 2. 处理时间 (Timestamp -> String)
        if (project.getCreateTime() != null) {
            lbCreateTime.setText(DateTimeFormat.getFormat("yyyy-MM-dd").format(project.getCreateTime()));
        }

        // 3. 应用项目主颜色 (Color)
        String themeColor = (project.getColor() != null && !project.getColor().isEmpty()) ? project.getColor() : "#4a90e2";
        pnlColorBar.getElement().getStyle().setBackgroundColor(themeColor);
        pnlIconBox.getElement().getStyle().setBackgroundColor(themeColor);

        // 4. 处理图标 (Icon 或 Unicode)
        // 如果有 unicode（通常是 FontAwesome 代码），则应用类名
        if (StringUtil.isNotBlank(project.getUnicode())) {
            iconElement.setClassName("fa " + project.getUnicode());
        } else {
            iconElement.setClassName("fa fa-project-diagram"); // 默认图标
        }
    }

    interface ProjectCardUiBinder extends UiBinder<HTMLPanel, ProjectCard> {
    }
}