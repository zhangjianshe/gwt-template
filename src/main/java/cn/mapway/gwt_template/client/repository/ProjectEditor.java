package cn.mapway.gwt_template.client.repository;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.db.VwProjectEntity;
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
import com.google.gwt.user.client.ui.Label;

/**
 * 项目创建
 */
public class ProjectEditor extends CommonEventComposite implements IData<VwProjectEntity> {
    private static final ProjectEditorUiBinder ourUiBinder = GWT.create(ProjectEditorUiBinder.class);
    private static Dialog<ProjectEditor> dialog;
    @UiField
    SaveBar saveBar;
    @UiField
    AiTextBox txtName;
    @UiField
    AiTextBox txtFullName;
    @UiField
    Label lbTip;
    private VwProjectEntity project;

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
        return new Dialog<>(editor, "创建项目");
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(600, 350);
    }

    @Override
    public VwProjectEntity getData() {
        return project;
    }

    @Override
    public void setData(VwProjectEntity obj) {
        project = obj;
        if (project == null) {
            project = new VwProjectEntity();
        }
        toUI();
    }

    private void toUI() {
        txtName.setValue(project.getName());
        txtFullName.setValue(project.getFullName());
    }

    boolean isNameValid(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return name.matches("^[a-zA-Z_][a-zA-Z0-9_-]*$");
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            DevProjectEntity temp = new DevProjectEntity();
            temp.setId(project.getId());
            temp.setName(txtName.getValue().trim());
            boolean nameValid = isNameValid(txtName.getValue().trim());
            if (!nameValid) {
                saveBar.msg(lbTip.getText());
                return;
            }
            temp.setFullName(txtFullName.getValue());
            doSave(temp);
        } else {
            fireEvent(event);
        }
    }

    private void doSave(DevProjectEntity temp) {
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setProject(temp);
        AppProxy.get().updateProject(request, new AsyncCallback<>() {
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