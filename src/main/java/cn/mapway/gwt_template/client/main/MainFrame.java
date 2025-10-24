package cn.mapway.gwt_template.client.main;

import cn.mapway.ui.client.widget.CommonEventComposite;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public class MainFrame extends CommonEventComposite {
    interface MainFrameUiBinder extends UiBinder<DockLayoutPanel, MainFrame> {
    }

    private static MainFrameUiBinder ourUiBinder = GWT.create(MainFrameUiBinder.class);

    public MainFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }
}