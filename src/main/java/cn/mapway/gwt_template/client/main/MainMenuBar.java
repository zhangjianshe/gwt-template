package cn.mapway.gwt_template.client.main;

import cn.mapway.gwt_template.client.dns.DnsFrame;
import cn.mapway.ui.client.mvc.SwitchModuleData;
import cn.mapway.ui.client.widget.CommonEventComposite;
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

    public MainMenuBar() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @UiHandler("btnDns")
    public void btnDnsClick(ClickEvent event) {
        SwitchModuleData switchModuleData = new SwitchModuleData(DnsFrame.MODULE_CODE, "");
        fireEvent(CommonEvent.switchEvent(switchModuleData));
    }

    interface MainMenuBarUiBinder extends UiBinder<HTMLPanel, MainMenuBar> {
    }
}