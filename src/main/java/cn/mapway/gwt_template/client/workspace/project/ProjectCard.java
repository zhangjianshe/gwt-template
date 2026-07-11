package cn.mapway.gwt_template.client.workspace.project;

import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.rpc.file.SecurityLevel;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.ui.client.mvc.IToolsProvider;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ProjectCard extends CommonEventComposite implements IToolsProvider, IData<DevProjectEntity> {
    private static final ProjectCardUiBinder ourUiBinder = GWT.create(ProjectCardUiBinder.class);
    @UiField
    HTMLPanel pnlName;
    @UiField
    HTMLPanel pnlSummary;
    @UiField
    Label lbCreator;
    @UiField
    com.google.gwt.dom.client.Element lbMemberCount;
    @UiField
    com.google.gwt.dom.client.Element lbProgress;
    @UiField
    InlineLabel lbCreateTime;
    @UiField
    Label lbSecurity;
    @UiField
    HTMLPanel card;
    DevProjectEntity project;

    public ProjectCard() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public DevProjectEntity getData() {
        return project;
    }

    @Override
    public void setData(DevProjectEntity obj) {
        project = obj;
        toUI();
    }



    private void toUI() {
        //秘密等级
        SecurityLevel securityLevel = SecurityLevel.fromRank(project.getSecurityLevel());

        lbSecurity.setText(securityLevel.getText());
        lbSecurity.getElement().getStyle().setColor(securityLevel.getColor());

        // 1. 基础文字设置
        pnlName.getElement().setInnerText(project.getName());
        pnlSummary.getElement().setInnerText(project.getSummary());
        lbCreator.setText(project.getCreateUserName() != null ? project.getCreateUserName() : "未知");
        lbMemberCount.setInnerText(String.valueOf(project.getMemberCount() != null ? project.getMemberCount() : 0));
        lbProgress.setInnerText(project.getProgress() != null ? project.getProgress() : "0%");

        // 2. 处理时间 (Timestamp -> String)
        if (project.getCreateTime() != null) {
            lbCreateTime.setText(DateTimeFormat.getFormat("yyyy-MM-dd").format(project.getCreateTime()));
        }

        // 3. 应用项目主颜色与背景
        String themeColor = (project.getColor() != null && !project.getColor().isEmpty()) ? project.getColor() : "#4a90e2";
        CommonPermission commonPermission = CommonPermission.from(project.getCurrentUserPermission());
        Style style = card.getElement().getStyle();

        if (StringUtil.isNotBlank(project.getIcon())) {
            style.setBackgroundImage("url('" + project.getIcon() + "')");
            style.clearProperty("borderTop"); // 移除顶部边框
        } else {
            style.clearBackgroundImage();
            style.setProperty("borderTop", "6px solid " + themeColor); // 巧用主题色作为顶部线条点缀，非常专业！
        }

    }


    @Override
    public Widget getTools() {
        return new Label();
    }


    interface ProjectCardUiBinder extends UiBinder<HTMLPanel, ProjectCard> {
    }
}