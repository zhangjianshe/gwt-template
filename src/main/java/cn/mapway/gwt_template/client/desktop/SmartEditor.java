package cn.mapway.gwt_template.client.desktop;

import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.PlusButton;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 * 智能编辑器
 */
public class SmartEditor extends CommonEventComposite implements IData<String> {
    private static final SmartEditorUiBinder ourUiBinder = GWT.create(SmartEditorUiBinder.class);
    @UiField
    PlusButton btnUpload;
    @UiField
    TextArea editor;
    @UiField
    HTMLPanel tools;
    @UiField
    HorizontalPanel toolbar;

    public SmartEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
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

    interface SmartEditorUiBinder extends UiBinder<HTMLPanel, SmartEditor> {
    }
}