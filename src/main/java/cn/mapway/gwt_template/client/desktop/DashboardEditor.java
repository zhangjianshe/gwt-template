package cn.mapway.gwt_template.client.desktop;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DashboardEntity;
import cn.mapway.gwt_template.shared.rpc.desktop.UpdateDashboardRequest;
import cn.mapway.gwt_template.shared.rpc.desktop.UpdateDashboardResponse;
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

public class DashboardEditor extends CommonEventComposite implements IData<DashboardEntity> {
    private static final DashboardEditorUiBinder ourUiBinder = GWT.create(DashboardEditorUiBinder.class);
    private static Dialog<DashboardEditor> dialog;
    @UiField
    SaveBar saveBar;
    @UiField
    AiTextBox txtName;
    @UiField
    AiTextBox txtRank;
    private DashboardEntity dashboard;

    public DashboardEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
        txtRank.asNumber();
    }

    public static Dialog<DashboardEditor> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<DashboardEditor> createOne() {
        return new Dialog<>(new DashboardEditor(), "编辑面板");
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(500, 300);
    }

    @Override
    public DashboardEntity getData() {
        return dashboard;
    }

    @Override
    public void setData(DashboardEntity obj) {
        dashboard = obj;
        if (obj == null) {
            dashboard = new DashboardEntity();
            dashboard.setName("新建面板");
            dashboard.setRank(9999.);
            dashboard.setLayout("[]");
            dashboard.setSummary("");
        }
        toUI();
    }

    private void toUI() {
        saveBar.msg("");
        txtName.setValue(dashboard.getName());
        txtRank.setValue(dashboard.getRank() + "");
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

    private void doSave() {
        UpdateDashboardRequest request = new UpdateDashboardRequest();
        request.setLayout(dashboard);
        AppProxy.get().saveDesktopLayout(request, new AsyncCallback<RpcResult<UpdateDashboardResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                saveBar.msg(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateDashboardResponse> result) {
                if (result.isSuccess()) {
                    fireEvent(CommonEvent.okEvent(result.getData().getDashboard()));
                } else {
                    saveBar.msg(result.getMessage());
                }
            }
        });
    }

    private void fromUI() {
        dashboard.setName(txtName.getValue());
        dashboard.setRank(Double.parseDouble(txtRank.getValue()));
    }


    interface DashboardEditorUiBinder extends UiBinder<DockLayoutPanel, DashboardEditor> {
    }
}