package cn.mapway.gwt_template.client.widget.gridstack;

import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

public class GridPanelBar extends CommonEventComposite {
    private static final GridPanelBarUiBinder ourUiBinder = GWT.create(GridPanelBarUiBinder.class);
    @UiField
    FontIcon btnDelete;
    @UiField
    FontIcon btnConfig;
    @UiField
    Label txtTitle;

    public GridPanelBar() {
        initWidget(ourUiBinder.createAndBindUi(this));
        btnDelete.setIconUnicode(Fonts.CLOSE1);
        btnConfig.setIconUnicode(Fonts.SETTING);
    }

    @UiHandler("btnDelete")
    public void btnDeleteClick(ClickEvent event) {
        fireEvent(CommonEvent.deleteEvent(null));
    }

    @UiHandler("btnConfig")
    public void btnConfigClick(ClickEvent event) {
        fireEvent(CommonEvent.configEvent(btnConfig));
    }

    public void setTitle(String title) {
        txtTitle.setText(title);
    }

    public void setDesignMode(boolean designMode) {
        btnConfig.setVisible(designMode);
        btnDelete.setVisible(designMode);
    }

    interface GridPanelBarUiBinder extends UiBinder<HTMLPanel, GridPanelBar> {
    }
}