package cn.mapway.gwt_template.client.project.basic;

import cn.mapway.gwt_template.client.project.member.MemberList;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.VwProjectEntity;
import cn.mapway.gwt_template.shared.rpc.project.ReadRepoFileRequest;
import cn.mapway.gwt_template.shared.rpc.project.ReadRepoFileResponse;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;

public class ProjectFlowPanel extends CommonEventComposite implements IData<VwProjectEntity> {
    private static final ProjectFlowPanelUiBinder ourUiBinder = GWT.create(ProjectFlowPanelUiBinder.class);
    @UiField
    MemberList memberList;
    @UiField
    HTML readme;
    @UiField
    ProjectDetailPanel detailPanel;
    @UiField
    HTMLPanel tipPanel;
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
        detailPanel.setData(project);
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
                } else if (result.getCode() == 404) {
                    //READ ME不存在　可以手动添加一个
                    tipPanel.setVisible(true);
                    tipPanel.clear();
                    Button btnAddReadme = new Button("添加README文件");
                    btnAddReadme.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            doAddReadmeFile();
                        }
                    });
                    tipPanel.add(btnAddReadme);
                    readme.setText("");
                } else {
                    readme.setHTML("<div class='ai-message'>" + result.getMessage() + "</div>");
                }
            }
        });
    }

    private void doAddReadmeFile() {

    }

    @UiHandler("detailPanel")
    public void detailPanelCommon(CommonEvent event) {
        if (event.isUpdate()) {
            VwProjectEntity temp = event.getValue();
            setData(temp);
            fireEvent(CommonEvent.updateEvent(temp));
        }
    }

    interface ProjectFlowPanelUiBinder extends UiBinder<DockLayoutPanel, ProjectFlowPanel> {
    }
}