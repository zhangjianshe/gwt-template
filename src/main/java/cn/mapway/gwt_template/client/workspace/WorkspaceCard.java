package cn.mapway.gwt_template.client.workspace;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.shared.db.DevWorkspaceEntity;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;

public class WorkspaceCard extends CommonEventComposite implements IData<DevWorkspaceEntity> {
    private static final WorkspaceCardUiBinder ourUiBinder = GWT.create(WorkspaceCardUiBinder.class);
    @UiField
    FontIcon icon;
    @UiField
    Label lbName;
    @UiField
    Label lbSummary;
    @UiField
    Label lbEdit;
    @UiField
    Image imgCover;
    @UiField
    Image imgAvatar;
    @UiField
    Label lbUserName;
    @UiField
    InlineLabel lbProjectCount;
    private DevWorkspaceEntity data;

    public WorkspaceCard() {
        initWidget(ourUiBinder.createAndBindUi(this));
        lbEdit.setText("\uD83D\uDCDD编辑");
        addDomHandler(event -> fireEvent(CommonEvent.selectEvent(data)), ClickEvent.getType());
    }

    @Override
    public DevWorkspaceEntity getData() {
        return data;
    }

    @Override
    public void setData(DevWorkspaceEntity obj) {
        data = obj;
        toUI();
    }

    private void toUI() {
        if (data == null) return;

        // 1. 设置名称和简介
        lbName.setText(StringUtil.trim(data.getName()));
        lbSummary.setText(StringUtil.isBlank(data.getSummary()) ? "暂无描述" : data.getSummary());

        // 2. 处理封面图 (使用你的 FLD_ICON)
        if (StringUtil.isNotBlank(data.getIcon())) {
            imgCover.setUrl(data.getIcon());
            imgCover.setVisible(true);
            icon.setVisible(false);
        } else {
            imgCover.setVisible(false);
            icon.setVisible(true);
            icon.setIconUnicode(Fonts.WORKSPACE); // 或者使用 data.getUnicode()
        }

        // 3. 处理新增辅助字段
        lbUserName.setText(StringUtil.isBlank(data.getUserName()) ? "未知用户" : data.getUserName());
        if (StringUtil.isNotBlank(data.getUserAvatar())) {
            imgAvatar.setUrl(data.getUserAvatar());
        }

        // 显示项目数量，如果没有数据则显示 0
        int count = data.getProjectCount() == null ? 0 : data.getProjectCount();
        lbProjectCount.setText(String.valueOf(count));

        // 4. 编辑权限
        lbEdit.setVisible(ClientContext.get().isCurrentUser(data.getUserId()));

        // 5. 设置主色调（可选，可以用 color 字段改变 lbName 的颜色或边框色）
        if (StringUtil.isNotBlank(data.getColor())) {
            lbName.getElement().getStyle().setColor(data.getColor());
        }
    }

    @UiHandler("lbEdit")
    public void btnEditClick(ClickEvent event) {
        event.stopPropagation();
        event.preventDefault();
        fireEvent(CommonEvent.editEvent(data));
    }


    interface WorkspaceCardUiBinder extends UiBinder<HTMLPanel, WorkspaceCard> {
    }
}