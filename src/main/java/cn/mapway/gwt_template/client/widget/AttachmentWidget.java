package cn.mapway.gwt_template.client.widget;

import cn.mapway.gwt_template.client.widget.file.CommonFileUploadResult;
import cn.mapway.ui.client.tools.JSON;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.DeleteButton;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import elemental2.dom.File;
import elemental2.dom.FormData;
import elemental2.dom.URL;
import elemental2.dom.XMLHttpRequest;
import jsinterop.base.Js;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

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
    File file;
    boolean transfering = false;
    Map<String, Object> datas = new HashMap<>();
    @Getter
    boolean error = false;
    @Setter
    private String actionUrl;

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
        this.file = file;
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

    public AttachmentWidget clearData() {
        datas.clear();
        return this;
    }

    public AttachmentWidget appendData(String key, Object value) {
        datas.put(key, value);
        return this;
    }

    /**
     * 执行带进度更新的上传
     */
    public void startTransfer() {
        if (transfering) {
            return;
        }
        transfering = true;
        XMLHttpRequest xhr = new XMLHttpRequest();

        // 核心：监听进度事件
        xhr.upload.onprogress = (evt) -> {
            if (evt.lengthComputable) {
                double percent = Math.round((evt.loaded / evt.total) * 100);
                // 找到对应的 card 更新进度
                Scheduler.get().scheduleDeferred(() -> setProgress(percent));
            }
        };

        // 核心：监听完成状态
        xhr.onload = (evt) -> {
            Scheduler.get().scheduleDeferred(() -> {
                transfering = false;
                if (xhr.status == 200) {
                    CommonFileUploadResult result = Js.uncheckedCast(JSON.parse(xhr.responseText));
                    if (result.code == 200) {
                        fireEvent(CommonEvent.uploadEvent(result.data));
                    } else {
                        error = true;
                        fireEvent(CommonEvent.errorEvent(result.message));
                    }
                } else {
                    // 上传失败
                    error = true;
                    fireEvent(CommonEvent.errorEvent(xhr.statusText));
                }
            });
        };

        // 3. 发送请求
        xhr.open("POST", actionUrl); // 替换为你的后端路径
        FormData formData = new FormData();
        formData.append("file", file); // 'file' 是后端接收的参数名
        if (datas != null) {
            for (Map.Entry<String, Object> entry : datas.entrySet()) {
                formData.append(entry.getKey(), FormData.AppendValueUnionType.of(entry.getValue()));
            }
        }
        xhr.send(formData);
    }

    interface AttachmentWidgetUiBinder extends UiBinder<FlowPanel, AttachmentWidget> {
    }
}