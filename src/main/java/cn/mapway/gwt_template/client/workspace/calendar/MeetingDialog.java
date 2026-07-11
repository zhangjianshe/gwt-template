package cn.mapway.gwt_template.client.workspace.calendar;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskResponse;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.util.StringUtil;
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
import com.google.gwt.user.client.ui.RequiresResize;

public class MeetingDialog extends CommonEventComposite implements RequiresResize {

    private static final MeetingDialogUiBinder ourUiBinder = GWT.create(MeetingDialogUiBinder.class);
    private static Dialog<MeetingDialog> dialog;
    @UiField
    SaveBar saveBar;
    @UiField
    MeetingPanel panel;
    @UiField
    DockLayoutPanel root;

    public MeetingDialog() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Dialog<MeetingDialog> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<MeetingDialog> createOne() {
        return new Dialog<>(new MeetingDialog(), "编辑大事记");
    }

    public void edit(DevProjectTaskEntity meet) {
        panel.setData(meet);
        boolean savable = StringUtil.isBlank(meet.getId())
                || ClientContext.get().isCurrentUser(meet.getCreateUserId())
                || ClientContext.get().isAdmin();
        panel.enableEdit(savable);
        saveBar.setEnableSave(savable);
    }

    public void enableEdit(boolean editable)
    {
        panel.enableEdit(editable);
        saveBar.setEnableSave(editable);
    }

    @Override
    public Size requireDefaultSize() {
        return ClientContext.getDialogSize();
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            DevProjectTaskEntity taskEntity = panel.fromUI();
            doSave(taskEntity);
        } else {
            fireEvent(event);
        }
    }

    private void doSave(DevProjectTaskEntity taskEntity) {

        UpdateProjectTaskRequest request = new UpdateProjectTaskRequest();
        request.setProjectTask(taskEntity);
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
    public void onResize() {
        root.onResize();
    }

    interface MeetingDialogUiBinder extends UiBinder<DockLayoutPanel, MeetingDialog> {
    }
}