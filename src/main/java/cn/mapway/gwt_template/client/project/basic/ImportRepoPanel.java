package cn.mapway.gwt_template.client.project.basic;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.user.ClientWebSocket;
import cn.mapway.gwt_template.client.user.GitNotifyMessage;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.VwProjectEntity;
import cn.mapway.gwt_template.shared.rpc.project.ImportRepoRequest;
import cn.mapway.gwt_template.shared.rpc.project.ImportRepoResponse;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.AiTextBox;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import elemental2.dom.DomGlobal;
import jsinterop.base.Js;

import java.util.Objects;

/**
 * 仓库导入面板
 */
public class ImportRepoPanel extends CommonEventComposite implements IData<VwProjectEntity> {
    private static final ImportRepoPanelUiBinder ourUiBinder = GWT.create(ImportRepoPanelUiBinder.class);
    @UiField
    AiTextBox txtUrl;
    @UiField
    AiButton btnImport;
    @UiField
    Label lbMessage;
    @UiField
    AiTextBox txtToken;
    @UiField
    AiTextBox txtAuthorize;
    private VwProjectEntity project;

    public ImportRepoPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
        ClientWebSocket.get().connectToServer();
    }

    void msg(String msg) {
        lbMessage.setText(msg);
    }

    @UiHandler("btnImport")
    public void btnImportClick(ClickEvent event) {
        String url = txtUrl.getText();
        if (StringUtil.isBlank(url)) {
            msg("没有录入仓库地址");
        }

        ImportRepoRequest request = new ImportRepoRequest();
        request.setProjectId(project.getId());
        request.setRepoUrl(txtUrl.getValue());
        request.setUser(txtAuthorize.getText());
        request.setTokenOrPassword(txtToken.getText());
        AppProxy.get().importRepo(request, new AsyncCallback<RpcResult<ImportRepoResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                lbMessage.setText(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<ImportRepoResponse> result) {
                if (result.isSuccess()) {
                    lbMessage.setText("start import");
                } else {
                    lbMessage.setText(result.getMessage());
                }
            }
        });
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        registerBusEvent(AppConstant.TOPIC_GIT_IMPORT);
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        clearBusEvent();
    }

    @Override
    public void onEvent(String topic, int type, Object event) {
        if (Objects.equals(topic, AppConstant.TOPIC_GIT_IMPORT)) {
            GitNotifyMessage info = Js.uncheckedCast(event);
            if (info.phase.equals(AppConstant.MESSAGE_PHASE_IMPORT)) {
                if (info.type.equals(AppConstant.MESSAGE_TYPE_SUCCESS)) {
                    DomGlobal.console.log("import success");
                    // 延迟一秒让用户看清“100%”，然后通知外部刷新
                    Timer timer = new Timer() {
                        @Override
                        public void run() {
                            fireEvent(CommonEvent.reloadEvent(project));
                        }
                    };
                    timer.schedule(1000);
                } else if (info.type.equals(AppConstant.MESSAGE_TYPE_ERROR)) {
                    DomGlobal.console.log("import error");
                    lbMessage.setText(info.message);
                } else if (info.type.equals(AppConstant.MESSAGE_TYPE_PROGRESS)) {
                    DomGlobal.console.log("import progress");
                    lbMessage.setText("目前进度" + info.progress + "%" + " " + info.message);
                }
            }
        }
    }

    @Override
    public VwProjectEntity getData() {
        return project;
    }

    @Override
    public void setData(VwProjectEntity obj) {
        project = obj;
        toUI();
    }

    private void toUI() {
        txtUrl.setValue("");
        txtAuthorize.setValue("");
        txtToken.setValue("");
        lbMessage.setText("");
    }

    interface ImportRepoPanelUiBinder extends UiBinder<HTMLPanel, ImportRepoPanel> {
    }
}