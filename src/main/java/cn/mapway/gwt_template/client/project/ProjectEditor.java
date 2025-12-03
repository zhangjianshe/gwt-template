package cn.mapway.gwt_template.client.project;

import cn.mapway.gwt_template.client.node.NodeListBox;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.rpc.dev.UpdateProjectRequest;
import cn.mapway.gwt_template.shared.rpc.dev.UpdateProjectResponse;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.AiTextBox;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public class ProjectEditor extends CommonEventComposite implements IData<DevProjectEntity> {
    private static final ProjectEditorUiBinder ourUiBinder = GWT.create(ProjectEditorUiBinder.class);
    private static Dialog<ProjectEditor> dialog;
    @UiField
    SaveBar saveBar;
    @UiField
    AiTextBox txtName;
    @UiField
    AiTextBox txtSource;
    @UiField
    NodeListBox ddlNodes;
    private DevProjectEntity project;

    public ProjectEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Dialog<ProjectEditor> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<ProjectEditor> createOne() {
        ProjectEditor editor = new ProjectEditor();
        return new Dialog<>(editor, "编辑项目");
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(800, 600);
    }

    @Override
    public DevProjectEntity getData() {
        return project;
    }

    @Override
    public void setData(DevProjectEntity obj) {
        project = obj;
        if (project == null) {
            project = new DevProjectEntity();
        }
        toUI();
    }

    private void toUI() {
        txtName.setValue(project.getName());
        txtSource.setValue(project.getSourceUrl());
        ddlNodes.setValue(project.getDeployServer());
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            DevProjectEntity temp = new DevProjectEntity();
            temp.setId(project.getId());
            temp.setName(txtName.getValue());
            temp.setSourceUrl(txtSource.getValue());
            temp.setDeployServer((String) ddlNodes.getValue());
            doSave(temp);
        } else {
            fireEvent(event);
        }
    }

    private void doSave(DevProjectEntity temp) {
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setProject(temp);
        AppProxy.get().updateProject(request, new AsyncCallback<RpcResult<UpdateProjectResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                saveBar.msg(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateProjectResponse> result) {
                if (result.isSuccess()) {
                    fireEvent(CommonEvent.okEvent(result.getData().getProject()));
                } else {
                    saveBar.msg(result.getMessage());
                }
            }
        });
    }


    interface ProjectEditorUiBinder extends UiBinder<DockLayoutPanel, ProjectEditor> {
    }
}