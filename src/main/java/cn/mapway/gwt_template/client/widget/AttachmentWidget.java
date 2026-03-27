package cn.mapway.gwt_template.client.widget;

import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.DeleteButton;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import elemental2.dom.File;
import elemental2.dom.URL;

/**
 * 附件
 */
public class AttachmentWidget extends CommonEventComposite {
    private static final AttachmentWidgetUiBinder ourUiBinder = GWT.create(AttachmentWidgetUiBinder.class);
    @UiField
    Image previewImage;
    @UiField
    Label fileNameLabel;
    @UiField
    FlowPanel progressOverlay;
    @UiField
    HTML progressInner;
    @UiField
    DeleteButton deleteButton;
    @UiField
    FlowPanel container;

    public AttachmentWidget() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    /**
     * 更新进度 (0-100)
     */
    public void setProgress(double percent) {
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;

        progressInner.getElement().getStyle().setWidth(percent, Style.Unit.PCT);

        // 当达到 100% 时，可以隐藏遮罩层或改变颜色
        if (percent >= 100) {
            progressOverlay.addStyleName("complete");
            deleteButton.setVisible(true); // 上传完成后允许删除
        } else {
            deleteButton.setVisible(false); // 上传期间不允许删除，防止状态错乱
        }
    }

    /**
     * 设置上传失败状态
     */
    public void setError(String message) {
        progressOverlay.addStyleName("error");
        fileNameLabel.setText("ERROR: " + message);
        deleteButton.setVisible(true); // 失败后允许删除
    }

    public void setFile(File file) {
        if (file.type.startsWith("image/")) {
            String objectUrl = URL.createObjectURL(file);
            previewImage.setUrl(objectUrl);
        } else {
            // 通用文件图标
            previewImage.setUrl("img/archive.svg"); // 替换为你的图标路径
        }
        setProgress(0);
    }

    @UiHandler("deleteButton")
    public void deleteButtonClick(ClickEvent event) {
        fireEvent(CommonEvent.deleteEvent(null));
    }

    interface AttachmentWidgetUiBinder extends UiBinder<FlowPanel, AttachmentWidget> {
    }
}