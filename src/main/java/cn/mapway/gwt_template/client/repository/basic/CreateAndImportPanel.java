package cn.mapway.gwt_template.client.repository.basic;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.user.GitNotifyMessage;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.repository.ImportRepoRequest;
import cn.mapway.gwt_template.shared.rpc.repository.ImportRepoResponse;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.util.StringUtil;
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
import jsinterop.base.Js;

import java.util.Objects;

/**
 * 创建仓库并导入
 */
public class CreateAndImportPanel extends CommonEventComposite {
    private static final CreateAndImportPanelUiBinder ourUiBinder = GWT.create(CreateAndImportPanelUiBinder.class);
    static Dialog<CreateAndImportPanel> dialog;
    @UiField
    SaveBar saveBar;
    @UiField
    AiTextBox txtUrl;
    @UiField
    AiTextBox txtAuthorize;
    @UiField
    AiTextBox txtToken;
    @UiField
    AiTextBox txtNewRepoName;

    public CreateAndImportPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
        txtUrl.addValueChangeHandler(event -> {
            String name = StringUtil.extractBaseName(txtUrl.getValue());
            txtNewRepoName.setValue(name);
        });
    }

    public static Dialog<CreateAndImportPanel> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return dialog;
        }
    }

    private static Dialog<CreateAndImportPanel> createOne() {
        CreateAndImportPanel panel = new CreateAndImportPanel();
        return new Dialog<>(panel, "从其他仓库导入");
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(800, 400);
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            doImport();
        } else {
            fireEvent(event);
        }
    }

    private void doImport() {
        registerBusEvent(AppConstant.TOPIC_GIT_IMPORT);
        ImportRepoRequest request = new ImportRepoRequest();
        request.setRepositoryId("");
        request.setRepoUrl(txtUrl.getValue());
        request.setUser(txtAuthorize.getValue());
        request.setTokenOrPassword(txtToken.getValue());
        request.setNewRepositoryName(txtNewRepoName.getValue());
        saveBar.setEnableSave(false);
        AppProxy.get().importRepo(request, new AsyncCallback<RpcResult<ImportRepoResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                saveBar.setEnableSave(true);
                saveBar.message(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<ImportRepoResponse> result) {
                if (result.isSuccess()) {
                    saveBar.message("开始导入");
                } else {
                    saveBar.setEnableSave(true);
                    saveBar.msg(result.getMessage());
                }
            }
        });
    }

    @Override
    public void onEvent(String topic, int type, Object event) {
        if (Objects.equals(topic, AppConstant.TOPIC_GIT_IMPORT)) {
            GitNotifyMessage info = Js.uncheckedCast(event);
            if (info.phase.equals(AppConstant.MESSAGE_PHASE_IMPORT)) {
                if (info.type.equals(AppConstant.MESSAGE_TYPE_SUCCESS)) {
                    saveBar.setEnableSave(true);
                    saveBar.message("导入完成");
                    fireEvent(CommonEvent.okEvent(null));
                } else if (info.type.equals(AppConstant.MESSAGE_TYPE_ERROR)) {
                    saveBar.message(info.message);
                } else if (info.type.equals(AppConstant.MESSAGE_TYPE_PROGRESS)) {
                    saveBar.message("目前进度" + info.progress + "%" + " " + info.message);
                }
            }
        }
    }

    interface CreateAndImportPanelUiBinder extends UiBinder<DockLayoutPanel, CreateAndImportPanel> {
    }
}