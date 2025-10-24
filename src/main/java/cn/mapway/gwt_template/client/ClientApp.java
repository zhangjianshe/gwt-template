package cn.mapway.gwt_template.client;

import cn.mapway.gwt_template.client.main.MainFrame;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootLayoutPanel;

public class ClientApp implements EntryPoint {
    public void onModuleLoad() {
        MainFrame mainFrame = new MainFrame();
        RootLayoutPanel.get().add(mainFrame);
    }
}
