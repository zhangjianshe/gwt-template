package cn.mapway.gwt_template.client.widget;

import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import elemental2.dom.DomGlobal;
import elemental2.dom.File;
import elemental2.dom.FileList;
import elemental2.dom.HTMLInputElement;
import jsinterop.base.JsArrayLike;

/**
 * 上传文件按钮
 */
public class Uploader extends CommonEventComposite {
    private static final UploaderUiBinder ourUiBinder = GWT.create(UploaderUiBinder.class);
    @UiField
    FontIcon btnUploader;
    HTMLInputElement fileInput;
    String action = "";
    String path = "";

    public Uploader() {
        initWidget(ourUiBinder.createAndBindUi(this));
        btnUploader.setIconUnicode(Fonts.UPLOAD_SMALL);
        initUploader();
    }

    private void initUploader() {
        // 创建一个内存中的 input 元素，不添加到 DOM
        fileInput = (HTMLInputElement) DomGlobal.document.createElement("input");
        fileInput.type = "file";
        fileInput.multiple = true; // 支持多选
        fileInput.accept = "image/*, .pdf, .txt"; // 限制类型

        // 监听文件选择事件
        fileInput.onchange = (e) -> {
            FileList files = fileInput.files;
            handleFiles(files);
            // 清空 value，保证同一个文件连续上传也能触发 change
            fileInput.value = "";
            return null;
        };
    }

    private void handleFiles(JsArrayLike<File> files) {
        if (files.getLength() == 0) {
            return;
        }
        Popup<UploaderProgressPanel> popup = UploaderProgressPanel.getPopup(true);
        popup.addCommonHandler(commonEvent -> {
            if (commonEvent.isUpload()) {
                fireEvent(CommonEvent.uploadEvent(commonEvent.getValue()));
                // check upload result
            } else if (commonEvent.isClose()) {
                popup.hide();
                btnUploader.setEnabled(true);
            }
        });
        popup.getContent().appendFiles(files);


        btnUploader.setEnabled(false);
        popup.showRelativeTo(btnUploader);
        popup.getContent().doUpload(action, path);
    }

    @UiHandler("btnUploader")
    public void btnUploaderClick(ClickEvent event) {
        fileInput.click();
    }

    public void setActionAndPath(String action, String path) {
        this.action = action;
        this.path = path;
    }

    public void upload(JsArrayLike<File> files) {
        handleFiles(files);
    }

    interface UploaderUiBinder extends UiBinder<HTMLPanel, Uploader> {
    }
}