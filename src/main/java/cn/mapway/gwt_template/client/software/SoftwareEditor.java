package cn.mapway.gwt_template.client.software;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.client.widget.TextBox;
import cn.mapway.gwt_template.shared.db.SysSoftwareEntity;
import cn.mapway.gwt_template.shared.rpc.soft.CreateSoftwareRequest;
import cn.mapway.gwt_template.shared.rpc.soft.CreateSoftwareResponse;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public class SoftwareEditor extends CommonEventComposite implements IData<SysSoftwareEntity> {
    private static final SoftwareEditorUiBinder ourUiBinder = GWT.create(SoftwareEditorUiBinder.class);
    public static Dialog<SoftwareEditor> dialog;
    @UiField
    SaveBar saveBar;
    @UiField
    TextBox txtName;
    @UiField
    TextBox txtSummary;
    private SysSoftwareEntity software;

    public SoftwareEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Dialog<SoftwareEditor> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();

        }
    }

    private static Dialog<SoftwareEditor> createOne() {
        SoftwareEditor editor = new SoftwareEditor();
        return new Dialog<>(editor, "编辑软件");
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(800, 550);
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            fromUI();
            saveBar.msg("开始保存");
            CreateSoftwareRequest request = new CreateSoftwareRequest();
            request.setSoftware(software);
            AppProxy.get().createSoftware(request, new AsyncAdaptor<RpcResult<CreateSoftwareResponse>>() {
                @Override
                public void onData(RpcResult<CreateSoftwareResponse> result) {
                    saveBar.msg("已保存");
                    fireEvent(CommonEvent.okEvent(result.getData().getSoftware()));
                }
            });
        } else {
            fireEvent(event);
        }
    }

    private void fromUI() {
        software.setName(txtName.getValue());
        software.setName(txtSummary.getValue());
    }

    @Override
    public SysSoftwareEntity getData() {
        return software;
    }

    @Override
    public void setData(SysSoftwareEntity obj) {
        software = obj;
        if (software == null) {
            software = new SysSoftwareEntity();
            software.setName("软件名称");
        }
        toUI();
    }

    private void toUI() {
        txtName.setValue(software.getName());
        txtSummary.setValue(software.getSummary());
    }

    interface SoftwareEditorUiBinder extends UiBinder<DockLayoutPanel, SoftwareEditor> {
    }
}