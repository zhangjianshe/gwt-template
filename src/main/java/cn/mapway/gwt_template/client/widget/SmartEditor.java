package cn.mapway.gwt_template.client.widget;

import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.PlusButton;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.TextArea;
import elemental2.dom.*;
import jsinterop.base.Js;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 智能编辑器
 */
public class SmartEditor extends CommonEventComposite implements IData<String> {
    private static final SmartEditorUiBinder ourUiBinder = GWT.create(SmartEditorUiBinder.class);
    static int ATTACHMENT_HEIGHT = 140;
    @UiField
    PlusButton btnUpload;
    @UiField
    TextArea editor;
    @UiField
    HTMLPanel tools;
    @UiField
    HorizontalPanel toolbar;
    @UiField
    FlowPanel attachmentPanel;
    @UiField
    HTMLPanel bottomPanel;
    @UiField
    DockLayoutPanel root;
    HTMLInputElement fileInput;
    @Setter
    String uploadUrl = "";

    Map<String, String> datas = new HashMap<>();

    public SmartEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
        initUploadFeature();
        initPasteSupport();
        editor.getElement().setAttribute("contenteditable", "true");
        editor.getElement().setAttribute("placeholder", "写点什么? Shift+Enter 换行编写");
        editor.addKeyDownHandler(event -> {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                if (event.getNativeEvent().getShiftKey()) {
                    editor.getElement().getStyle().setLineHeight(150, Style.Unit.PCT);
                    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                        @Override
                        public void execute() {
                            //改变自身大小
                            int scrollHeight = editor.getElement().getScrollHeight();
                            int totalHeight = scrollHeight + 80;
                            fireEvent(CommonEvent.resizeEvent(new Size(0, totalHeight)));
                        }
                    });
                } else {
                    event.preventDefault();
                    event.stopPropagation();
                    fireEvent(CommonEvent.okEvent(editor.getValue().replace("\n", "\n\n")));
                }
            }
        });
    }

    public SmartEditor clearUploadData() {
        datas.clear();
        return this;
    }

    public SmartEditor appendUploadData(String key, String value) {
        datas.put(key, value);
        return this;
    }

    private void initUploadFeature() {
        // 创建一个内存中的 input 元素，不添加到 DOM
        fileInput = (HTMLInputElement) DomGlobal.document.createElement("input");
        fileInput.type = "file";
        fileInput.multiple = true; // 支持多选
        fileInput.accept = "image/*, .pdf, .txt"; // 限制类型

        // 监听文件选择事件
        fileInput.onchange = (e) -> {
            FileList files = fileInput.files;
            if (files != null && files.length > 0) {
                handleFiles(files);
            }
            // 清空 value，保证同一个文件连续上传也能触发 change
            fileInput.value = "";
            return null;
        };
    }

    private void initPasteSupport() {
        // 将 GWT 的 Element 转为 Elemental2 的 HTMLElement
        elemental2.dom.HTMLElement el = Js.uncheckedCast(editor.getElement());

        el.addEventListener("paste", evt -> {
            elemental2.dom.ClipboardEvent cbEvent = (elemental2.dom.ClipboardEvent) evt;
            elemental2.dom.DataTransferItemList items = cbEvent.clipboardData.items;

            for (int i = 0; i < items.length; i++) {
                elemental2.dom.DataTransferItem item = items.getAt(i);
                if (item.type.contains("image")) {
                    File file = item.getAsFile();
                    DomGlobal.console.log("检测到粘贴图片: " + file.name);
                    // 执行上传逻辑...
                }
            }
        });
    }

    private void handleFiles(FileList files) {
        for (int i = 0; i < files.length; i++) {
            File file = files.item(i);
            DomGlobal.console.log("准备上传文件: " + file.name + " (" + file.size + " bytes)");
            btnUpload.setEnabled(false); // 简单处理：上传期间禁用按钮
            // 1. 立即创建一个预览卡片并加入 UI
            AttachmentWidget card = new AttachmentWidget();
            card.addCommonHandler(commonEvent -> {
                if (commonEvent.isDelete()) {
                    card.removeFromParent();
                    checkAttachmentBar();
                }
            });
            card.setFile(file);
            attachmentPanel.add(card);
            // 2. 执行真正的 XHR 上传，并将 card 传递过去以便更新
            doUploadWithProgress(file, card);
        }

        if (files.length > 0) {
            attachmentPanel.setHeight(ATTACHMENT_HEIGHT + "px");
            int height = root.getOffsetHeight() + ATTACHMENT_HEIGHT + 80;
            root.setWidgetSize(bottomPanel, ATTACHMENT_HEIGHT);
            fireEvent(CommonEvent.resizeEvent(new Size(0, height)));
        }
    }

    private void onUploadSuccess(String fileName, String returnData, AttachmentWidget card) {
        fireEvent(CommonEvent.uploadEvent(returnData));
        card.removeFromParent();
        checkAttachmentBar();
    }

    private void checkAttachmentBar() {
        if (attachmentPanel.getWidgetCount() == 0) {
            root.setWidgetSize(bottomPanel, 0);
            int height = Math.max(160, root.getOffsetHeight() - ATTACHMENT_HEIGHT - 80);
            root.setWidgetSize(bottomPanel, 0);
            fireEvent(CommonEvent.resizeEvent(new Size(0, height)));
            btnUpload.setEnabled(true);
        }
    }

    private void onUploadError(String fileName, String error, AttachmentWidget card) {
        DomGlobal.console.error(fileName + " 上传失败: " + error);
        card.setError(error);
    }

    /**
     * 执行带进度更新的上传
     */
    private void doUploadWithProgress(File file, AttachmentWidget card) {
        XMLHttpRequest xhr = new XMLHttpRequest();

        // 核心：监听进度事件
        xhr.upload.onprogress = (evt) -> {
            if (evt.lengthComputable) {
                double percent = Math.round((evt.loaded / evt.total) * 100);
                // 找到对应的 card 更新进度
                Scheduler.get().scheduleDeferred(() -> card.setProgress(percent));
            }
        };

        // 核心：监听完成状态
        xhr.onload = (evt) -> {
            Scheduler.get().scheduleDeferred(() -> {
                if (xhr.status == 200) {
                    // 上传成功: xhr.responseText 通常是服务器返回的文件 URL
                    onUploadSuccess(file.name, xhr.responseText, card);
                } else {
                    // 上传失败
                    onUploadError(file.name, "Server Error: " + xhr.status, card);
                }
            });
        };

        // 3. 发送请求
        xhr.open("POST", uploadUrl); // 替换为你的后端路径
        FormData formData = new FormData();
        formData.append("file", file); // 'file' 是后端接收的参数名
        if (datas != null) {
            for (Map.Entry<String, String> entry : datas.entrySet()) {
                formData.append(entry.getKey(), entry.getValue());
            }
        }
        xhr.send(formData);
    }

    public void appendTool(Widget tools) {
        toolbar.clear();
        toolbar.add(tools);
    }

    public void reset() {
        editor.getElement().getStyle().setLineHeight(100, Style.Unit.PCT);
    }

    @Override
    public String getData() {
        return editor.getValue();
    }

    @Override
    public void setData(String obj) {
        editor.setValue(obj);
    }

    @UiHandler("btnUpload")
    public void btnUploadClick(ClickEvent event) {
        fileInput.click();
    }

    interface SmartEditorUiBinder extends UiBinder<DockLayoutPanel, SmartEditor> {
    }
}