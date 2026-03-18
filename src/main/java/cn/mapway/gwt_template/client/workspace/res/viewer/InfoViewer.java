package cn.mapway.gwt_template.client.workspace.res.viewer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;

public class InfoViewer extends Composite implements RequiresResize {
    private static final InfoViewerUiBinder ourUiBinder = GWT.create(InfoViewerUiBinder.class);
    @UiField
    Label header;
    @UiField
    Label msg;
    @UiField
    DockLayoutPanel root;

    public InfoViewer() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public void setMessage(String headerText, String msg) {
        header.setText(headerText);
        this.msg.setText(msg);
    }

    @Override
    public void onResize() {
        root.onResize();
    }

    interface InfoViewerUiBinder extends UiBinder<DockLayoutPanel, InfoViewer> {
    }
}