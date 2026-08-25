package cn.mapway.gwt_template.client.docker;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DockerAppEntity;
import cn.mapway.gwt_template.shared.rpc.docker.UpdateDockerAppRequest;
import cn.mapway.gwt_template.shared.rpc.docker.UpdateDockerAppResponse;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.AiTextBox;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public class EditDockerAppDialog extends CommonEventComposite implements IData<DockerAppEntity> {
    private static final EditDockerAppDialogUiBinder ourUiBinder = GWT.create(EditDockerAppDialogUiBinder.class);
    private static Dialog<EditDockerAppDialog> dialog;
    @UiField
    SaveBar saveBar;
    @UiField
    AiButton btnSelect;
    @UiField
    AiTextBox txtDir;
    @UiField
    AiTextBox txtName;
    private DockerAppEntity appEntity;

    public EditDockerAppDialog() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Dialog<EditDockerAppDialog> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<EditDockerAppDialog> createOne() {
        return new Dialog<>(new EditDockerAppDialog(), "编辑应用");
    }

    @UiHandler("btnSelect")
    public void btnSelectClick(ClickEvent event) {
        Dialog<SysDirExplorer> dialog1 = SysDirExplorer.getDialog(true);
        dialog1.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isSelect()) {
                    String path = event.getValue();
                    txtDir.setValue(path);
                }
                dialog1.hide();
            }
        });
        dialog1.getContent().loadPath(txtDir.getValue());
        dialog1.center();
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            saveBar.msg("saving...");
            formUI();
            doSave();
        } else {
            fireEvent(event);
        }
    }

    private void doSave() {
        UpdateDockerAppRequest request = new UpdateDockerAppRequest();
        request.setAppEntity(appEntity);
        AppProxy.get().updateDockerApp(request, new AsyncCallback<RpcResult<UpdateDockerAppResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                saveBar.msg(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateDockerAppResponse> result) {
                if (result.isSuccess()) {
                    fireEvent(CommonEvent.updateEvent(result.getData().getAppEntity()));
                } else {
                    saveBar.msg(result.getMessage());
                }
            }
        });
    }

    private void formUI() {
        appEntity.setName(txtName.getValue());
        appEntity.setAbsolutePath(txtDir.getValue());
    }

    @Override
    public DockerAppEntity getData() {
        return appEntity;
    }

    @Override
    public void setData(DockerAppEntity obj) {
        if (obj == null) {
            obj = new DockerAppEntity();
            obj.setName("应用名称");
            obj.setAbsolutePath("/");
        }
        appEntity = obj;
        toUI();
    }

    private void toUI() {
        txtName.setValue(appEntity.getName());
        txtDir.setValue(appEntity.getAbsolutePath());
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(500, 280);
    }

    interface EditDockerAppDialogUiBinder extends UiBinder<DockLayoutPanel, EditDockerAppDialog> {
    }
}