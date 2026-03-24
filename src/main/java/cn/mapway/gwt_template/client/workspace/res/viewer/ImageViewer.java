package cn.mapway.gwt_template.client.workspace.res.viewer;

import cn.mapway.gwt_template.shared.rpc.project.module.PreviewData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.LayoutPanel;

public class ImageViewer extends Composite {
    private static final ImageViewerUiBinder ourUiBinder = GWT.create(ImageViewerUiBinder.class);
    @UiField
    Image image;

    public ImageViewer() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public void setData(PreviewData data) {
        image.setUrl(data.getBody());
    }

    interface ImageViewerUiBinder extends UiBinder<LayoutPanel, ImageViewer> {
    }
}