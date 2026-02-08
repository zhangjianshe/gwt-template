package cn.mapway.gwt_template.client.project.basic;

import cn.mapway.gwt_template.shared.db.VwProjectEntity;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.AiTextBox;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import cn.mapway.ui.client.widget.dialog.Popup;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import elemental2.dom.DomGlobal;

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
        // 根据当前协议生成地址，例如：http://domain/git/owner/repo.git
        String baseUrl = GWT.getHostPageBaseURL(); // 获取当前域名

        String cloneUrl = baseUrl + "code/"
                + project.getOwnerName() + "/" + project.getName() + ".git";
        txtHttp.setValue(cloneUrl);

        String cleanUrl = baseUrl.replace("http://", "").replace("https://", "");
        if (cleanUrl.endsWith("/")) {
            cleanUrl = cleanUrl.substring(0, cleanUrl.length() - 1);
        }
        String sshUrl = "git@" + cleanUrl + ":" + project.getOwnerName() + "/" + project.getName() + ".git";
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
                        msgLabel.setText("拷贝成功");
                        return null;
                    })
                    .catch_(err -> {
                        DomGlobal.console.error("Clipboard Error:", err);
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

    interface ClonePanelUiBinder extends UiBinder<TabLayoutPanel, ClonePanel> {
    }
}