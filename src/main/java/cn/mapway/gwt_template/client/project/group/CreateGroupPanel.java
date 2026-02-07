package cn.mapway.gwt_template.client.project.group;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DevGroupEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevGroupRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevGroupResponse;
import cn.mapway.ui.client.mvc.Size;
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

public class CreateGroupPanel extends CommonEventComposite {
    private static final CreateGroupPanelUiBinder ourUiBinder = GWT.create(CreateGroupPanelUiBinder.class);
    private static Dialog<CreateGroupPanel> dialog;
    @UiField
    SaveBar saveBar;
    @UiField
    AiTextBox txtName;
    @UiField
    AiTextBox txtFullName;

    public CreateGroupPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Dialog<CreateGroupPanel> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<CreateGroupPanel> createOne() {
        CreateGroupPanel panel = new CreateGroupPanel();
        return new Dialog<>(panel, "创建开发组");
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            doSave();
        } else {
            fireEvent(event);
        }
    }

    private void doSave() {
        UpdateDevGroupRequest request = new UpdateDevGroupRequest();
        DevGroupEntity group = new DevGroupEntity();
        group.setName(txtName.getValue());
        group.setFullName(txtFullName.getValue());
        request.setDevGroup(group);
        AppProxy.get().updateDevGroup(request, new AsyncCallback<>() {
            @Override
            public void onFailure(Throwable caught) {
                saveBar.msg(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateDevGroupResponse> result) {
                if (result.isSuccess()) {
                    fireEvent(CommonEvent.updateEvent(result.getData()));
                } else {
                    saveBar.msg(result.getMessage());
                }
            }
        });
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(600, 350);
    }

    public void reset() {
        txtFullName.setValue("");
        txtName.setValue("");
    }

    interface CreateGroupPanelUiBinder extends UiBinder<DockLayoutPanel, CreateGroupPanel> {
    }

}