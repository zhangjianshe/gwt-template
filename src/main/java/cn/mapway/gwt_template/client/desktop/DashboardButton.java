package cn.mapway.gwt_template.client.desktop;

import cn.mapway.gwt_template.shared.db.DashboardEntity;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

public class DashboardButton extends CommonEventComposite implements IData<DashboardEntity> {
    private static final DashboardButtonUiBinder ourUiBinder = GWT.create(DashboardButtonUiBinder.class);
    @UiField
    HTMLPanel root;
    @UiField
    Label btnName;
    private DashboardEntity entity;

    public DashboardButton() {
        initWidget(ourUiBinder.createAndBindUi(this));
        addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fireEvent(CommonEvent.selectEvent(entity));
            }
        }, ClickEvent.getType());
    }

    @Override
    public DashboardEntity getData() {
        return entity;
    }

    @Override
    public void setData(DashboardEntity obj) {
        entity = obj;
        toUI();
    }

    private void toUI() {
        btnName.setText(entity.getName());
    }

    interface DashboardButtonUiBinder extends UiBinder<HTMLPanel, DashboardButton> {
    }
}