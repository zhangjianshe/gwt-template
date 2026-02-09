package cn.mapway.gwt_template.client.project.basic;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.preference.PreferenceFrame;
import cn.mapway.gwt_template.client.preference.key.UserPublicKeyFrame;
import cn.mapway.gwt_template.shared.db.VwProjectEntity;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.AiTextBox;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.shared.CommonConstant;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import elemental2.dom.DomGlobal;
import elemental2.dom.URL;

public class ClonePanel extends CommonEventComposite implements IData<VwProjectEntity> {
    private static final ClonePanelUiBinder ourUiBinder = GWT.create(ClonePanelUiBinder.class);
    private static Popup<ClonePanel> popup;
    @UiField
    FontIcon btnCopyHttp;
    @UiField
    AiTextBox txtHttp;
    @UiField
    Label lbMessage;
    @UiField
    AiTextBox txtSSH;
    @UiField
    FontIcon btnCopySSH;
    @UiField
    Label lbMessage2;
    @UiField
    TabLayoutPanel tab;
    @UiField
    Anchor btnConfig;
    private VwProjectEntity project;

    public ClonePanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
        btnCopyHttp.setIconUnicode(Fonts.COPY);
        btnCopySSH.setIconUnicode(Fonts.COPY);
        tab.addSelectionHandler(event -> {
            lbMessage.setText("");
            lbMessage2.setText("");
        });
    }

    public static Popup<ClonePanel> getPopup(boolean reuse) {
        if (reuse) {
            if (popup == null) {
                popup = createOne();
            }
            return popup;
        } else {
            return createOne();
        }
    }

    private static Popup<ClonePanel> createOne() {
        ClonePanel panel = new ClonePanel();
        return new Popup<>(panel);
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
        if (project == null) {
            return;
        }

        // --- HTTP Clone URL ---
        String baseUrl = GWT.getHostPageBaseURL();
        String cloneUrl = baseUrl + "code/"
                + project.getOwnerName() + "/" + project.getName() + ".git";
        txtHttp.setValue(cloneUrl);

        // --- SSH Clone URL ---
        URL url = new URL(baseUrl);
        Integer sshPort = ClientContext.get().getAppData().getSshPort();

        // Most professional Git hosts use 'git' as the SSH user
        String sshUser = "git";

        String sshUrl;
        if (sshPort != null && sshPort != 22) {
            // Use the explicit protocol format for custom ports
            // Format: ssh://git@domain:2222/owner/project.git
            sshUrl = "ssh://" + sshUser + "@" + url.hostname + ":" + sshPort + "/"
                    + project.getOwnerName() + "/" + project.getName() + ".git";
        } else {
            // Standard format: git@domain:owner/project.git
            sshUrl = sshUser + "@" + url.hostname + ":"
                    + project.getOwnerName() + "/" + project.getName() + ".git";
        }

        txtSSH.setValue(sshUrl);
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(450, 150);
    }

    private void copyToClipboard(String text, Label msgLabel) {
        if (DomGlobal.navigator.clipboard != null) {
            DomGlobal.navigator.clipboard.writeText(text)
                    .then(p -> {
                        msgLabel.setText("已复制");
                        msgLabel.addStyleName("ai-success"); // Make it green!

                        // Clear message after 3 seconds
                        new com.google.gwt.user.client.Timer() {
                            @Override
                            public void run() {
                                msgLabel.setText("");
                            }
                        }.schedule(3000);
                        return null;
                    });
        } else {
            msgLabel.setText("浏览器不支持自动复制，请手动选中复制");
        }
    }

    @UiHandler("btnCopyHttp")
    public void btnCopyHttpClick(ClickEvent event) {
        copyToClipboard(txtHttp.getValue(), lbMessage);
    }

    @UiHandler("btnCopySSH")
    public void btnCopySSHClick(ClickEvent event) {
        copyToClipboard(txtSSH.getValue(), lbMessage2);
    }

    @UiHandler("btnConfig")
    public void btnConfigClick(ClickEvent event) {
        fireEvent(CommonEvent.closeEvent(null));
        Dialog<PreferenceFrame> dialog = PreferenceFrame.getDialog(true);
        dialog.addCommonHandler(event1 -> {
            if (event1.isClose()) {
                dialog.hide();
            }
        });
        ModuleParameter initParam = new ModuleParameter();
        initParam.put(CommonConstant.KEY_INIT_MODULE_CODE, UserPublicKeyFrame.MODULE_CODE);
        dialog.getContent().initialize(null, initParam);
        dialog.center();
    }


    interface ClonePanelUiBinder extends UiBinder<TabLayoutPanel, ClonePanel> {
    }
}