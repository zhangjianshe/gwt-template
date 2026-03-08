package cn.mapway.gwt_template.client.workspace;

import cn.mapway.gwt_template.shared.db.DevWorkspaceEntity;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.FontIcon;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

public class WorkspaceCard extends Composite implements IData<DevWorkspaceEntity> {
    private static final WorkspaceCardUiBinder ourUiBinder = GWT.create(WorkspaceCardUiBinder.class);
    @UiField
    FontIcon icon;
    @UiField
    Label lbName;
    @UiField
    Label lbSummary;
    @UiField
    Label lbTime;
    @UiField
    Label lbStatus;
    private DevWorkspaceEntity data;
    public WorkspaceCard() {
        initWidget(ourUiBinder.createAndBindUi(this));
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

        // 设置图标和基础信息
        icon.setIconUnicode(Fonts.WORKSPACE);
        lbName.setText(StringUtil.trim(data.getName()));
        lbSummary.setText(StringUtil.isBlank(data.getSummary()) ? "暂无描述信息" : data.getSummary());

        // 格式化时间
        if (data.getCreateTime() != null) {
            lbTime.setText(StringUtil.formatDate(data.getCreateTime(), "yyyy-MM-dd"));
        }

        // 状态标识
        if (Boolean.TRUE.equals(data.getIsShare())) {
            lbStatus.setText("👥 共享");
            lbStatus.getElement().getStyle().setColor("#52c41a");
        } else {
            lbStatus.setText("🔒 私有");
            lbStatus.getElement().getStyle().setColor("#bfbfbf");
        }
    }

    interface WorkspaceCardUiBinder extends UiBinder<HTMLPanel, WorkspaceCard> {
    }
}