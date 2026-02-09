package cn.mapway.gwt_template.client.project.webhook;

import cn.mapway.gwt_template.shared.db.WebHookEntity;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.Header;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

public class WebHookItem extends CommonEventComposite implements IData<WebHookEntity> {
    private static final WebHookItemUiBinder ourUiBinder = GWT.create(WebHookItemUiBinder.class);

    @UiField
    Label lblStatus;
    @UiField
    Label lblUrl;
    @UiField
    Label lblEvents;
    @UiField
    Label lblMethod;
    @UiField
    SStyle style;
    @UiField
    Header lblFilter;
    @UiField
    AiButton btnRunning;

    private WebHookEntity data;

    public WebHookItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @UiHandler("btnEdit")
    void onEdit(ClickEvent e) {
        fireEvent(CommonEvent.editEvent(data));
    }

    @UiHandler("btnDelete")
    void onDelete(ClickEvent e) {
        fireEvent(CommonEvent.deleteEvent(data));
    }

    @UiHandler("btnRunning")
    public void btnRunningClick(ClickEvent event) {
        fireEvent(CommonEvent.selectEvent(data));
    }

    @Override
    public WebHookEntity getData() {
        return data;
    }

    @Override
    public void setData(WebHookEntity obj) {
        this.data = obj;

        lblUrl.setText(obj.getTargetUrl());
        lblMethod.setText(obj.getMethod() == null ? "POST" : obj.getMethod());
        lblEvents.setText(obj.getSourceEvent() == null ? "push" : obj.getSourceEvent());
        btnRunning.setText("运行次数  " + (obj.getActiveCount() == null ? 0 : obj.getActiveCount()));
        lblFilter.setText(obj.getSourceFilter());
        // Status Logic
        if (Boolean.TRUE.equals(obj.getActive())) {
            lblStatus.addStyleName(style.active());
            lblStatus.setTitle("Active Data Link");
        } else {
            lblStatus.addStyleName(style.inactive());
            lblStatus.setTitle("Link Disabled");
        }
    }

    interface SStyle extends CssResource {
        String active();

        String inactive();

        String itemRow();

        String count();

        String m6();

        String ind();
    }

    interface WebHookItemUiBinder extends UiBinder<HTMLPanel, WebHookItem> {
    }
}