package cn.mapway.gwt_template.client.main;

import cn.mapway.gwt_template.client.dns.DnsFrame;
import cn.mapway.gwt_template.client.preference.PreferenceFrame;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.mvc.SwitchModuleData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * 窗口菜单栏
 */
public class MainMenuBar extends CommonEventComposite {
    private static final MainMenuBarUiBinder ourUiBinder = GWT.create(MainMenuBarUiBinder.class);
    @UiField
    Button btnDns;
    @UiField
    FontIcon btnPreference;

    public MainMenuBar() {
        initWidget(ourUiBinder.createAndBindUi(this));
        btnPreference.setIconUnicode(Fonts.SETTING);
    }

    @UiHandler("btnDns")
    public void btnDnsClick(ClickEvent event) {
        SwitchModuleData switchModuleData = new SwitchModuleData(DnsFrame.MODULE_CODE, "");
        fireEvent(CommonEvent.switchEvent(switchModuleData));
    }

    @UiHandler("btnPreference")
    public void btnPreferenceClick(ClickEvent event) {
        Dialog<PreferenceFrame> dialog = PreferenceFrame.getDialog(true);
        dialog.addCommonHandler(event1 -> {
            if (event1.isClose()) {
                dialog.hide();
            }
        });
        dialog.getContent().initialize(null, new ModuleParameter());
        dialog.center();
    }

    interface MainMenuBarUiBinder extends UiBinder<HTMLPanel, MainMenuBar> {
    }
}