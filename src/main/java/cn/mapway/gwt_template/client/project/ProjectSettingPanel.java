package cn.mapway.gwt_template.client.project;

import cn.mapway.gwt_template.client.project.webhook.WebHookConfigPanel;
import cn.mapway.gwt_template.shared.db.VwProjectEntity;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.list.List;
import cn.mapway.ui.client.widget.list.ListItem;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.LayoutPanel;

/**
 * 项目设置面板
 */
public class ProjectSettingPanel extends CommonEventComposite implements IData<VwProjectEntity> {
    private static final ProjectSettingPanelUiBinder ourUiBinder = GWT.create(ProjectSettingPanelUiBinder.class);
    @UiField
    LayoutPanel root;
    @UiField
    List list;
    WebHookConfigPanel webHookConfigPanel;
    private VwProjectEntity project;

    public ProjectSettingPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
        for (ProjectConfigPageEnum e : ProjectConfigPageEnum.values()) {
            ListItem item = new ListItem();
            item.setData(e);
            item.setIcon(e.unicode);
            item.setText(e.name);
            list.addItem(item);
        }
    }

    @UiHandler("list")
    public void listCommon(CommonEvent event) {
        if (event.isSelect()) {
            root.clear();
            ListItem listItem = event.getValue();
            ProjectConfigPageEnum projectConfigPageEnum = (ProjectConfigPageEnum) listItem.getData();
            switch (projectConfigPageEnum) {
                case CONFIG_WEBHOOK:
                    if (webHookConfigPanel == null) {
                        webHookConfigPanel = new WebHookConfigPanel();
                    }
                    root.add(webHookConfigPanel);
                    root.setWidgetTopBottom(webHookConfigPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
                    root.setWidgetLeftRight(webHookConfigPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
                    webHookConfigPanel.setData(project);
                    break;
            }

        }
    }

    @Override
    public VwProjectEntity getData() {
        return project;
    }

    @Override
    public void setData(VwProjectEntity obj) {
        project = obj;
        toUI();
    }

    private void toUI() {
        list.selectFirst(true);
    }

    interface ProjectSettingPanelUiBinder extends UiBinder<DockLayoutPanel, ProjectSettingPanel> {
    }
}