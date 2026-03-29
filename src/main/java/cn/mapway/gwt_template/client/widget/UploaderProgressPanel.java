package cn.mapway.gwt_template.client.widget;

import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import elemental2.dom.File;
import jsinterop.base.JsArrayLike;


public class UploaderProgressPanel extends CommonEventComposite {
    private static final UploaderProgressPanelUiBinder ourUiBinder = GWT.create(UploaderProgressPanelUiBinder.class);
    private static Popup<UploaderProgressPanel> popup = null;
    @UiField
    HorizontalPanel list;

    public UploaderProgressPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Popup<UploaderProgressPanel> getPopup(boolean reuse) {
        if (reuse) {
            if (popup == null) {
                popup = createOne();
            }
            return popup;
        } else {
            return createOne();
        }
    }

    private static Popup<UploaderProgressPanel> createOne() {
        return new Popup<>(new UploaderProgressPanel());
    }

    public void appendFiles(JsArrayLike<File> files) {
        for (int i = 0; i < files.getLength(); i++) {
            appendFile(files.getAt(i));
        }
    }

    public void appendFile(File file) {
        AttachmentWidget attachmentWidget = new AttachmentWidget();
        attachmentWidget.addCommonHandler(commonEvent -> {
            if (commonEvent.isDelete()) {
                attachmentWidget.removeFromParent();
                checkAttachmentBar();
            } else if (commonEvent.isUpload()) {
                attachmentWidget.removeFromParent();
                //上传完毕
                fireEvent(CommonEvent.uploadEvent(commonEvent.getValue()));
                checkAttachmentBar();
            } else if (commonEvent.isError()) {
                //
                checkAttachmentBar();
            }
        });
        attachmentWidget.setFile(file);
        list.add(attachmentWidget);
    }

    private void checkAttachmentBar() {

        for (int i = 0; i < list.getWidgetCount(); i++) {
            Widget widget = list.getWidget(i);
            if (widget instanceof AttachmentWidget) {
                AttachmentWidget attachmentWidget = (AttachmentWidget) widget;
                if (!attachmentWidget.isError()) {
                    attachmentWidget.startTransfer();
                    break;
                }
            }
        }
        if (list.getWidgetCount() == 0) {
            fireEvent(CommonEvent.closeEvent(null));
        }
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(480, 120);
    }

    public void doUpload(String action, String path) {
        for (int i = 0; i < list.getWidgetCount(); i++) {
            Widget widget = list.getWidget(i);
            if (widget instanceof AttachmentWidget) {
                AttachmentWidget attachmentWidget = (AttachmentWidget) widget;
                attachmentWidget.setActionUrl(action);
                attachmentWidget.clearData().appendData("path", path);
            }
        }
        checkAttachmentBar();
    }

    interface UploaderProgressPanelUiBinder extends UiBinder<DockLayoutPanel, UploaderProgressPanel> {
    }
}