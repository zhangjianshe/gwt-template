package cn.mapway.gwt_template.client.desktop;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.shared.db.DesktopItemEntity;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.DeleteButton;
import cn.mapway.ui.client.widget.buttons.EditButton;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class DesktopItem extends CommonEventComposite implements IData<DesktopItemEntity> {
    private static final DesktopItemUiBinder ourUiBinder = GWT.create(DesktopItemUiBinder.class);
    @UiField
    Label lbName;
    @UiField
    Image image;
    @UiField
    EditButton btnEditor;
    @UiField
    DeleteButton btnDelete;
    @UiField
    HTMLPanel btnPan;
    boolean needShowTools = false;
    private DesktopItemEntity data;

    public DesktopItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
        addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fireEvent(CommonEvent.clickEvent(data));
            }
        }, ClickEvent.getType());
        addDomHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                event.stopPropagation();
                event.preventDefault();
                if (needShowTools) {
                    btnPan.setVisible(true);
                }
            }
        }, MouseOverEvent.getType());

        addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                event.stopPropagation();
                event.preventDefault();
                if (needShowTools) {
                    btnPan.setVisible(false);
                }
            }
        }, MouseOutEvent.getType());
    }

    @Override
    public DesktopItemEntity getData() {
        return data;
    }

    @Override
    public void setData(DesktopItemEntity obj) {
        data = obj;
        toUI();
    }

    private void toUI() {
        lbName.setText(data.getName());
        if (StringUtil.isNotBlank(data.getIcon())) {
            image.setUrl(data.getIcon());
        }

        needShowTools = ClientContext.get().isCurrentUser(data.getUserId());
    }


    @UiHandler("btnEditor")
    public void btnEditorClick(ClickEvent event) {
        event.stopPropagation();
        event.preventDefault();
        fireEvent(CommonEvent.editEvent(data));
    }

    @UiHandler("btnDelete")
    public void btnDeleteClick(ClickEvent event) {
        event.stopPropagation();
        event.preventDefault();
        fireEvent(CommonEvent.deleteEvent(data));
    }

    interface DesktopItemUiBinder extends UiBinder<HTMLPanel, DesktopItem> {
    }
}