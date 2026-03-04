package cn.mapway.gwt_template.client.repository.basic;

import cn.mapway.gwt_template.shared.db.VwRepositoryEntity;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.FontIcon;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

public class CloneButton extends Composite implements IData<VwRepositoryEntity> {

    private static final CloneButtonUiBinder ourUiBinder = GWT.create(CloneButtonUiBinder.class);
    @UiField
    FontIcon fontKind;
    @UiField
    FontIcon drop;
    @UiField
    Label lbName;

    public CloneButton() {
        initWidget(ourUiBinder.createAndBindUi(this));
        lbName.setText("Code");
        fontKind.setIconUnicode(Fonts.CODE);
        drop.setIconUnicode(Fonts.DOWN);
        addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showPopup();
            }
        },ClickEvent.getType());
    }

    private void showPopup() {
        Popup<ClonePanel> popup = ClonePanel.getPopup(true);
        popup.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                popup.hide();
            }
        });
        popup.getContent().setData(project);
        popup.showRelativeTo(this);
    }

    VwRepositoryEntity project;
    @Override
    public VwRepositoryEntity getData() {
        return project;
    }

    @Override
    public void setData(VwRepositoryEntity obj) {
        project=obj;
        toUI();
    }

    private void toUI() {
    }

    interface CloneButtonUiBinder extends UiBinder<HTMLPanel, CloneButton> {
    }
}