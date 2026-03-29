package cn.mapway.gwt_template.client.widget;

import cn.mapway.gwt_template.client.widget.file.UploadData;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.TextArea;
import elemental2.core.JsArray;
import elemental2.dom.File;
import elemental2.dom.FileList;
import jsinterop.base.Js;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 智能编辑器
 */
public class SmartEditor extends CommonEventComposite implements IData<String> {
    private static final SmartEditorUiBinder ourUiBinder = GWT.create(SmartEditorUiBinder.class);
    @UiField
    Uploader btnUpload;
    @UiField
    TextArea editor;
    @UiField
    HTMLPanel tools;
    @UiField
    HorizontalPanel toolbar;
    @UiField
    DockLayoutPanel root;

    Map<String, String> datas = new HashMap<>();

    public SmartEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
        initPasteSupport();
        initDragAndDropSupport();
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

    private void initDragAndDropSupport() {
        // 将 GWT Element 转为 Elemental2 的 HTMLElement
        elemental2.dom.HTMLElement el = Js.uncheckedCast(editor.getElement());

        // 1. 必须监听 dragover 并阻止默认行为，否则 drop 事件不会触发
        el.addEventListener("dragover", evt -> {
            evt.stopPropagation();
            evt.preventDefault();

            // 可选：添加一个视觉反馈，比如改变边框颜色
            el.style.backgroundColor = "rgba(0,0,0,0.05)";
        });

        // 2. 监听 dragleave 恢复样式
        el.addEventListener("dragleave", evt -> {
            el.style.backgroundColor = "";
        });

        // 3. 处理放置事件
        el.addEventListener("drop", evt -> {
            elemental2.dom.DragEvent dragEvt = (elemental2.dom.DragEvent) evt;

            evt.stopPropagation();
            evt.preventDefault();
            el.style.backgroundColor = ""; // 恢复样式

            if (dragEvt.dataTransfer != null && dragEvt.dataTransfer.files.length > 0) {
                FileList files = dragEvt.dataTransfer.files;
                btnUpload.upload(files);
            }
        });
    }

    private void initPasteSupport() {
        // 将 GWT 的 Element 转为 Elemental2 的 HTMLElement
        elemental2.dom.HTMLElement el = Js.uncheckedCast(editor.getElement());

        el.addEventListener("paste", evt -> {
            elemental2.dom.ClipboardEvent cbEvent = (elemental2.dom.ClipboardEvent) evt;
            elemental2.dom.DataTransferItemList items = cbEvent.clipboardData.items;

            JsArray<File> files = new JsArray<>();
            for (int i = 0; i < items.length; i++) {
                elemental2.dom.DataTransferItem item = items.getAt(i);
                if (Objects.equals(item.kind.toLowerCase(), "file")) {
                    files.push(item.getAsFile());
                }
            }
            if (files.length > 0) {
                evt.stopPropagation();
                evt.preventDefault();
                btnUpload.upload(files);
            }
        });
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
    public void btnUploadCommon(CommonEvent event) {
        if (event.isUpload()) {
            UploadData result = Js.uncheckedCast(event.getValue());
            String data = editor.getValue();
            String link = "";
            if (result.mime != null && result.mime.startsWith("image/")) {
                link = "\n\n![" + result.fileName + "](<" + result.relPath + ">)";
            } else {
                link = "\n\n[" + result.fileName + "](<" + result.relPath + ">)";
            }
            editor.setValue(data + link);
        }
    }

    public void setActionAndPath(String defaultUploadLocation, String path) {
        btnUpload.setActionAndPath(defaultUploadLocation, path);
    }

    interface SmartEditorUiBinder extends UiBinder<DockLayoutPanel, SmartEditor> {
    }
}