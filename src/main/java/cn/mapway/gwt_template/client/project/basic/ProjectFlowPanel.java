package cn.mapway.gwt_template.client.project.basic;

import cn.mapway.gwt_template.client.project.member.MemberList;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.VwProjectEntity;
import cn.mapway.gwt_template.shared.rpc.project.ReadRepoFileRequest;
import cn.mapway.gwt_template.shared.rpc.project.ReadRepoFileResponse;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;

public class ProjectFlowPanel extends CommonEventComposite implements IData<VwProjectEntity> {
    private static final ProjectFlowPanelUiBinder ourUiBinder = GWT.create(ProjectFlowPanelUiBinder.class);
    @UiField
    MemberList memberList;
    @UiField
    HTML readme;
    private VwProjectEntity project;

    public ProjectFlowPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public VwProjectEntity getData() {
        return project;
    }

    @Override
    public void setData(VwProjectEntity obj) {
        if (obj != null && obj != project) {
            project = obj;
            toUI();
        }
    }

    private void toUI() {
        memberList.setData(project.getId());
        loadReadme(project.getId());
    }

    private void loadReadme(String projectId) {
        readme.setHTML("loadding");
        ReadRepoFileRequest request = new ReadRepoFileRequest();
        request.setProjectId(projectId);
        request.setFilePathName("README.md");
        request.setToHtml(true);
        AppProxy.get().readRepoFile(request, new AsyncCallback<RpcResult<ReadRepoFileResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                readme.setHTML("<div class='ai-message'>" + caught.getMessage() + "</div>");
            }

            @Override
            public void onSuccess(RpcResult<ReadRepoFileResponse> result) {
                if (result.isSuccess()) {
                    readme.setHTML(result.getData().getText());
                } else {
                    readme.setHTML("<div class='ai-message'>" + result.getMessage() + "</div>");
                }
            }
        });
    }

    interface ProjectFlowPanelUiBinder extends UiBinder<DockLayoutPanel, ProjectFlowPanel> {
    }
}