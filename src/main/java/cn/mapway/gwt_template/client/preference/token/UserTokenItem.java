package cn.mapway.gwt_template.client.preference.token;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.rbac.shared.db.postgis.RbacTokenEntity;
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
import elemental2.dom.DomGlobal;

public class UserTokenItem extends CommonEventComposite implements IData<RbacTokenEntity> {
    private static final UserTokenItemUiBinder ourUiBinder = GWT.create(UserTokenItemUiBinder.class);

    @UiField
    AiButton btnDelete;
    @UiField
    AiButton btnCopy;
    @UiField
    FontIcon tokenIcon;
    @UiField
    Label lbSummary;
    @UiField
    Label lbToken;
    @UiField
    Label lbCreateTime;
    @UiField
    Label lbExpireTime;

    private RbacTokenEntity data;

    public UserTokenItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
        tokenIcon.setIconUnicode(Fonts.KEY);
        lbToken.addStyleName("ai-code");
    }

    @Override
    public RbacTokenEntity getData() {
        return data;
    }

    @Override
    public void setData(RbacTokenEntity obj) {
        data = obj;
        toUI();
    }

    private void toUI() {
        lbSummary.setText(StringUtil.isBlank(data.getSummary()) ? "未命名令牌" : data.getSummary());
        lbToken.setText(data.getId());
        lbCreateTime.setText(StringUtil.formatDate(data.getCreateTime()));
        if (data.getExpireTime() == null) {
            lbExpireTime.setText("永久有效");
        } else {
            lbExpireTime.setText(StringUtil.formatDate(data.getExpireTime()));
        }
    }

    @UiHandler("btnDelete")
    public void btnDeleteClick(ClickEvent event) {
        fireEvent(CommonEvent.deleteEvent(data));
    }

    @UiHandler("btnCopy")
    public void btnCopyClick(ClickEvent event) {
        String token = data.getId();
        if (DomGlobal.navigator.clipboard != null) {
            DomGlobal.navigator.clipboard.writeText(token).then(p -> {
                ClientContext.get().toast(0, 0, "令牌已复制");
                return null;
            });
        } else {
            ClientContext.get().toast(0, 0, "浏览器不支持自动复制，请手动复制");
        }
    }

    interface UserTokenItemUiBinder extends UiBinder<HTMLPanel, UserTokenItem> {
    }
}
