package cn.mapway.gwt_template.client.workspace;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DevWorkspaceFolderEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevWorkspaceFolderRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevWorkspaceFolderResponse;
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

public class WorkspaceFolderEditor extends CommonEventComposite implements IData<DevWorkspaceFolderEntity> {
    private static final WorkspaceFolderEditorUiBinder ourUiBinder = GWT.create(WorkspaceFolderEditorUiBinder.class);
    private static Dialog<WorkspaceFolderEditor> dialog;
    DevWorkspaceFolderEntity folder;
    @UiField
    SaveBar saveBar;
    @UiField
    AiTextBox txtColor;
    @UiField
    AiTextBox txtName;

    public WorkspaceFolderEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
        txtColor.asColor();
    }

    public static Dialog<WorkspaceFolderEditor> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }

    }

    public static Dialog<WorkspaceFolderEditor> createOne() {
        return new Dialog<>(new WorkspaceFolderEditor(), "编辑目录");
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(450, 260);
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
        txtColor.setValue(folder.getColor());
        txtName.setValue(folder.getName());
    }

    private void fromUI() {
        folder.setColor(txtColor.getValue());
        folder.setName(txtName.getValue());

    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            fromUI();
            UpdateDevWorkspaceFolderRequest request = new UpdateDevWorkspaceFolderRequest();
            request.setFolder(folder);
            AppProxy.get().updateDevWorkspaceFolder(request, new AsyncCallback<RpcResult<UpdateDevWorkspaceFolderResponse>>() {
                @Override
                public void onFailure(Throwable caught) {
                    saveBar.msg(caught.getMessage());
                }

                @Override
                public void onSuccess(RpcResult<UpdateDevWorkspaceFolderResponse> result) {
                    if (result.isSuccess()) {
                        fireEvent(CommonEvent.updateEvent(result.getData().getFolder()));
                    } else {
                        saveBar.msg(result.getMessage());
                    }
                }
            });
        } else {
            fireEvent(event);
        }
    }

    interface WorkspaceFolderEditorUiBinder extends UiBinder<DockLayoutPanel, WorkspaceFolderEditor> {
    }
}