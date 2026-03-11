package cn.mapway.gwt_template.client.workspace.task;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectMember;
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

/**
 * 任务编辑器
 */
public class DevTaskEditor extends CommonEventComposite implements IData<DevProjectTaskEntity> {
    private static final DevTaskEditorUiBinder ourUiBinder = GWT.create(DevTaskEditorUiBinder.class);
    private static Dialog<DevTaskEditor> dialog;
    @UiField
    SaveBar saveBar;
    @UiField
    AiTextBox txtName;
    @UiField
    ProjectMemberWidget member;
    private DevProjectTaskEntity task;

    public DevTaskEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Dialog<DevTaskEditor> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<DevTaskEditor> createOne() {
        return new Dialog<>(new DevTaskEditor(), "任务编辑");
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(800, 450);
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            DevProjectTaskEntity temp = new DevProjectTaskEntity();
            temp.setId(task.getId());
            temp.setName(txtName.getText());
            temp.setProjectId(task.getProjectId());
            temp.setCharger(task.getCharger());
            doSave(temp);
        } else {
            fireEvent(event);
        }
    }

    @UiHandler("member")
    public void memberCommon(CommonEvent event) {
        if (event.isSelect()) {
            ProjectMember projectMember = event.getValue();
            task.setCharger(projectMember.getUserId());
        }
    }

    private void doSave(DevProjectTaskEntity task) {
        UpdateProjectTaskRequest request = new UpdateProjectTaskRequest();
        request.setProjectTask(task);
        AppProxy.get().updateProjectTask(request, new AsyncCallback<RpcResult<UpdateProjectTaskResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                saveBar.msg(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateProjectTaskResponse> result) {
                if (result.isSuccess()) {
                    fireEvent(CommonEvent.updateEvent(result.getData().getProjectTask()));
                } else {
                    saveBar.msg(result.getMessage());
                }
            }
        });
    }

    @Override
    public DevProjectTaskEntity getData() {
        return task;
    }

    @Override
    public void setData(DevProjectTaskEntity obj) {
        task = obj;
        toUI();
    }

    private void toUI() {
        txtName.setText(task.getName());
        member.setData(task.getProjectId(), task.getCharger(), task.getChargeUserName(), task.getChargeAvatar());
    }

    interface DevTaskEditorUiBinder extends UiBinder<DockLayoutPanel, DevTaskEditor> {
    }
}