package cn.mapway.gwt_template.client.workspace.widget;

import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasValue;

import java.util.Objects;

public class SingleCheck extends CommonEventComposite implements HasValue<Object> {
    private static final SingleCheckUiBinder ourUiBinder = GWT.create(SingleCheckUiBinder.class);
    @UiField
    SStyle style;
    @UiField
    HTMLPanel root;
    AiButton selected = null;
    boolean enabled = true;
    ClickHandler itemClicked = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            if (!enabled) {
                return;
            }
            selectButton((AiButton) event.getSource(), true);
        }
    };

    public SingleCheck() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public void clear() {
        selected = null;
        root.clear();
    }

    private void selectButton(AiButton button, boolean fireEvent) {
        if (selected != null) {
            selected.setSelect(false);
            selected = null;
        }
        selected = button;
        if (selected != null) {
            selected.setSelect(true);
            if (fireEvent) {
                fireEvent(CommonEvent.selectEvent(selected.getData()));
            }
        }
    }

    public void addItem(String text, Object data) {
        AiButton b = new AiButton(text);
        b.setStyleName(style.option_item());
        b.addClickHandler(itemClicked);
        b.setData(data);
        root.add(b);
    }

    public void addHtmlItem(String html, Object data) {
        AiButton b = new AiButton();
        b.getElement().setInnerHTML(html);
        b.setStyleName(style.option_item());
        b.addClickHandler(itemClicked);
        b.setData(data);
        root.add(b);
    }

    @Override
    public Object getValue() {
        return selected == null ? null : selected.getData();
    }

    @Override
    public void setValue(Object value) {
        setValue(value, false);
    }

    @Override
    public void setValue(Object value, boolean fireEvents) {
        if (value == null) {
            selectButton(null, fireEvents);
        }
        for (int i = 0; i < root.getWidgetCount(); i++) {
            AiButton button = (AiButton) root.getWidget(i);
            if (Objects.equals(button.getData(), value)) {
                selectButton(button, fireEvents);
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Object> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    interface SingleCheckUiBinder extends UiBinder<HTMLPanel, SingleCheck> {
    }

    interface SStyle extends CssResource {

        String option_item();

        String option_group();
    }
}