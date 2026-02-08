package cn.mapway.gwt_template.client.preference.key;

import cn.mapway.gwt_template.shared.db.SysUserKeyEntity;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

public class PublicKeyItem extends CommonEventComposite implements IData<SysUserKeyEntity> {
    private static final PublicKeyItemUiBinder ourUiBinder = GWT.create(PublicKeyItemUiBinder.class);
    @UiField
    AiButton btnDelete;
    @UiField
    FontIcon keyIcon;
    @UiField
    Label lbName;
    @UiField
    Label lbAddTime;
    @UiField
    Label lbLastUsed;
    @UiField
    Label lbFigure;
    @UiField
    AiButton btnEdit;
    private SysUserKeyEntity data;

    public PublicKeyItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
        keyIcon.setIconUnicode(Fonts.KEY);
    }

    @Override
    public SysUserKeyEntity getData() {
        return data;
    }

    @Override
    public void setData(SysUserKeyEntity obj) {
        data = obj;
        toUI();
    }

    private void toUI() {
        lbName.setText(data.getName());
        lbAddTime.setText("创建时间:" + StringUtil.formatDate(data.getCreateTime()));
        lbFigure.setText(data.getKey());
        lbLastUsed.setText("最后使用时间:" + StringUtil.formatDate(data.getLastUsed()));
    }

    @UiHandler("btnDelete")
    public void btnDeleteClick(ClickEvent event) {
        fireEvent(CommonEvent.deleteEvent(data));
    }

    @UiHandler("btnEdit")
    public void btnEditClick(ClickEvent event) {
        fireEvent(CommonEvent.editEvent(data));
    }

    interface PublicKeyItemUiBinder extends UiBinder<HTMLPanel, PublicKeyItem> {
    }
}