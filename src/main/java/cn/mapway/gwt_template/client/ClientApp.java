package cn.mapway.gwt_template.client;

import cn.mapway.gwt_template.client.main.MainFrame;
import cn.mapway.ui.client.resource.MapwayResource;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootLayoutPanel;

public class ClientApp implements EntryPoint {
    public void onModuleLoad() {
        MapwayResource.INSTANCE.css().ensureInjected();
        MainFrame mainFrame = new MainFrame();
        RootLayoutPanel.get().add(mainFrame);
    }
}
