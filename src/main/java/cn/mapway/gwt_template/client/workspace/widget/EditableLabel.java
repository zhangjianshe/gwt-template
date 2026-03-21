package cn.mapway.gwt_template.client.workspace.widget;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import elemental2.dom.HTMLElement;
import jsinterop.base.Js;

public class EditableLabel extends Label implements HasValue<String> {
    private String oldText = "";

    public EditableLabel(String text) {
        super(text);
        init();
    }

    public EditableLabel() {
        super();
        init();
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        oldText = text;
    }

    private void init() {
        HTMLElement element = Js.uncheckedCast(getElement());
        element.setAttribute("contentEditable", "true");
        // 1. Capture the "Enter" key to prevent new lines in a title
        addDomHandler(event -> {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                event.preventDefault(); // 阻止换行
                getElement().blur(); // 触发失去焦点以保存
            }
        }, KeyDownEvent.getType());

        // 2. Save the data when the user clicks away
        addDomHandler(event -> {
            String newTitle = getText().trim();

            if (!newTitle.isEmpty() && !newTitle.equals(oldText)) {
                ValueChangeEvent.fire(EditableLabel.this, newTitle);
            }
        }, BlurEvent.getType());
    }

    @Override
    public String getValue() {
        return getText().trim();
    }

    @Override
    public void setValue(String value) {
        setText(value);
    }

    @Override
    public void setValue(String value, boolean fireEvents) {
        setText(value);
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}
