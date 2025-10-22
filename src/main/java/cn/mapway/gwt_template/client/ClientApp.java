package cn.mapway.gwt_template.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.*;

public class ClientApp implements EntryPoint {
    public void onModuleLoad() {
        RootLayoutPanel.get().add(new Label("Hello World!"));
    }
}
