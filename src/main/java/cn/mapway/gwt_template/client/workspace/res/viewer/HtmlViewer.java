package cn.mapway.gwt_template.client.workspace.res.viewer;

import cn.mapway.ui.client.mvc.IToolsProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

public class HtmlViewer extends Composite implements IToolsProvider {
    private static final HtmlViewerUiBinder ourUiBinder = GWT.create(HtmlViewerUiBinder.class);
    @UiField
    HTMLPanel container;
    @UiField
    HorizontalPanel tools;
    @UiField
    Anchor download;

    public HtmlViewer() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }


    public void setHtml(String html, String url) {
        container.clear();
        container.add(new HTML(html));
        download.setHref(url);
    }

    @Override
    public Widget getTools() {
        return tools;
    }

    interface HtmlViewerUiBinder extends UiBinder<LayoutPanel, HtmlViewer> {
    }
}