package cn.mapway.gwt_template.client.desktop;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DesktopItemEntity;
import cn.mapway.gwt_template.shared.rpc.desktop.UpdateDesktopRequest;
import cn.mapway.gwt_template.shared.rpc.desktop.UpdateDesktopResponse;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.AiTextBox;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.ImageUploader;
import cn.mapway.ui.client.widget.buttons.AiCheckBox;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.UploadReturn;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public class DesktopEditor extends CommonEventComposite implements IData<DesktopItemEntity> {
    private static final DesktopEditorUiBinder ourUiBinder = GWT.create(DesktopEditorUiBinder.class);
    private static Dialog<DesktopEditor> dialog;
    DesktopItemEntity data;
    @UiField
    AiTextBox txtName;
    @UiField
    ImageUploader uploader;
    @UiField
    AiTextBox txtSummary;
    @UiField
    AiTextBox txtData;
    @UiField
    AiTextBox txtRank;
    @UiField
    SaveBar saveBar;
    @UiField
    AiCheckBox checkShare;

    public DesktopEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
        txtRank.asNumber();
        uploader.setAction(GWT.getHostPageBaseURL() + "fileUpload", "desktop");
    }

    public static Dialog<DesktopEditor> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<DesktopEditor> createOne() {
        DesktopEditor editor = new DesktopEditor();
        return new Dialog<>(editor, "编辑快捷方式");
    }

    @Override
    public DesktopItemEntity getData() {
        return data;
    }

    @Override
    public void setData(DesktopItemEntity obj) {
        data = obj;
        if (data == null) {
            data = new DesktopItemEntity();
            data.setShare(false);
            data.setName("名称");
            data.setKind(0);
            data.setRank(0);
        }
        toUI();
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(800, 600);
    }

    private void toUI() {
        txtName.setValue(data.getName());
        uploader.setUrl(data.getIcon());
        txtData.setValue(data.getData());
        txtSummary.setValue(data.getSummary());
        txtRank.setValue(String.valueOf(data.getRank()));
        checkShare.setValue(data.getShare());
        checkShare.setEnabled(ClientContext.get().isAdmin());
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            fromUI();
            doSave();
        } else {
            fireEvent(event);
        }
    }

    @UiHandler("uploader")
    public void uploaderCommon(CommonEvent event) {
        if (event.isOk()) {
            UploadReturn uploadReturn = event.getValue();
            data.setIcon(uploadReturn.relPath);
        }
    }

    private void doSave() {
        UpdateDesktopRequest request = new UpdateDesktopRequest();
        request.setItem(data);
        saveBar.msg("保存中");
        AppProxy.get().updateDesktop(request, new AsyncCallback<RpcResult<UpdateDesktopResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                saveBar.msg(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateDesktopResponse> result) {
                if (result.isSuccess()) {
                    fireEvent(CommonEvent.updateEvent(result.getData().getItem()));
                    saveBar.msg("已保存");
                } else {
                    saveBar.msg(result.getMessage());
                }
            }
        });
    }

    private void fromUI() {
        data.setRank(Integer.parseInt(txtRank.getValue()));
        data.setSummary(txtSummary.getValue());
        data.setData(txtData.getValue());
        data.setIcon(uploader.getUrl());
        data.setName(txtName.getValue());
        data.setShare(checkShare.getValue());
    }

    interface DesktopEditorUiBinder extends UiBinder<DockLayoutPanel, DesktopEditor> {
    }
}