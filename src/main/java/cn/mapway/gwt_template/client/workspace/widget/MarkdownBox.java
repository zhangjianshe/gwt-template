package cn.mapway.gwt_template.client.workspace.widget;

import cn.mapway.ace.client.AceCommandDescription;
import cn.mapway.ace.client.AceEditor;
import cn.mapway.ace.client.AceEditorCallback;
import cn.mapway.ace.client.AceEditorMode;
import cn.mapway.gwt_template.client.js.markdown.MarkdownConvert;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasValue;
import lombok.Setter;

public class MarkdownBox extends CommonEventComposite implements HasValue<String> {
    private static final MarkdownViewerUiBinder ourUiBinder = GWT.create(MarkdownViewerUiBinder.class);
    String data;
    AceEditor aceEditor;
    Boolean viewMode = true;
    @UiField
    HTML body;
    @UiField
    HTMLPanel root;
    MarkdownConvert convert;
    boolean initialized = false;
    @Setter
    String tip = "";

    public MarkdownBox() {
        initWidget(ourUiBinder.createAndBindUi(this));
        convert = new MarkdownConvert();
        root.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (getEnabled()) {
                    setViewMode(false);
                }
            }
        }, ClickEvent.getType());
    }

    @Override
    public String getValue() {
        return data;
    }

    @Override
    public void setValue(String value) {
        setValue(value, false);
        toUI();
    }

    private void toUI() {
        if (viewMode) {
            viewData();
        } else {
            aceEditor.setValue(data);
        }
    }

    private void viewData() {
        if (StringUtil.isBlank(data) && StringUtil.isNotBlank(tip) && getEnabled()) {
            body.setHTML("<p style='padding:20px;color:#f0f0f0;font-size:2rem;text-align:center;'>" + tip + "</p>");
        } else {
            body.setHTML(convert.makeHtml(data));
        }
    }

    public void setViewMode(boolean viewMode) {
        if (this.viewMode != viewMode) {
            this.viewMode = viewMode;
            changeView();
        }
    }

    private void changeView() {
        if (viewMode) {
            if (aceEditor != null && aceEditor.isVisible()) {
                aceEditor.setVisible(false);
            }
            body.setVisible(true);
            viewData();
            ValueChangeEvent.fire(this, getValue());
        } else {
            body.setVisible(false);
            initEditor();
            aceEditor.setVisible(true);
            aceEditor.setValue(data);
            int width = root.getOffsetWidth() - 20;
            int height = root.getOffsetHeight() - 20;
            aceEditor.setPixelSize(width, height);
            aceEditor.redisplay();
        }
    }

    private void initEditor() {
        if (!initialized) {
            initialized = true;
            aceEditor = new AceEditor();
            Style style = aceEditor.getElement().getStyle();
            style.setPosition(Style.Position.ABSOLUTE);
            style.setProperty("inset", "10px");
            aceEditor.startEditor();
            aceEditor.setMode(AceEditorMode.MARKDOWN);
            aceEditor.setFontSize("1.2rem");
            aceEditor.setUseWorker(true);
            aceEditor.setShowGutter(false);
            aceEditor.setUseWrapMode(true);
            AceCommandDescription ctrlSaveCommand =
                    new AceCommandDescription("save", aceEditor -> {
                        doSave();
                        return true;
                    });
            ctrlSaveCommand.withBindKey("Ctrl-S", "Cmd-S");
            aceEditor.addCommand(ctrlSaveCommand);
            aceEditor.addEvent("blur", new AceEditorCallback() {
                @Override
                public void invokeAceCallback(JavaScriptObject javaScriptObject) {
                    doSave();
                }
            });
            root.add(aceEditor);
            int width = root.getOffsetWidth() - 20;
            int height = root.getOffsetHeight() - 20;
            aceEditor.setPixelSize(width, height);
            aceEditor.redisplay();
        }
    }

    private void doSave() {
        data = aceEditor.getValue();
        setViewMode(true);
    }

    @Override
    public void setValue(String value, boolean fireEvents) {
        data = value;
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    interface MarkdownViewerUiBinder extends UiBinder<HTMLPanel, MarkdownBox> {
    }
}