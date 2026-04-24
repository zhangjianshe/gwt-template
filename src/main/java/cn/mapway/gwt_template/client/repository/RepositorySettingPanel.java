package cn.mapway.gwt_template.client.repository;

import cn.mapway.gwt_template.client.repository.operation.RepositoryOperationPanel;
import cn.mapway.gwt_template.client.repository.webhook.WebHookConfigPanel;
import cn.mapway.gwt_template.shared.db.VwRepositoryEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.webhook.WebHookSourceKind;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.list.List;
import cn.mapway.ui.client.widget.list.ListItem;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
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
public class RepositorySettingPanel extends CommonEventComposite implements IData<VwRepositoryEntity> {
    private static final RepositorySettingPanelUiBinder ourUiBinder = GWT.create(RepositorySettingPanelUiBinder.class);
    @UiField
    LayoutPanel root;
    @UiField
    List list;
    WebHookConfigPanel webHookConfigPanel;
    RepositoryOperationPanel repositoryOperationPanel;
    CommonEventHandler repositoryOperationPanelHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent event) {
            if (event.isReload()) {
                fireEvent(CommonEvent.reloadEvent(null));
            }
        }
    };
    private VwRepositoryEntity repository;

    public RepositorySettingPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
        for (RepositoryConfigPageEnum e : RepositoryConfigPageEnum.values()) {
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
            RepositoryConfigPageEnum repositoryConfigPageEnum = (RepositoryConfigPageEnum) listItem.getData();
            switch (repositoryConfigPageEnum) {
                case CONFIG_WEBHOOK:
                    if (webHookConfigPanel == null) {
                        webHookConfigPanel = new WebHookConfigPanel();
                    }
                    root.add(webHookConfigPanel);
                    root.setWidgetTopBottom(webHookConfigPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
                    root.setWidgetLeftRight(webHookConfigPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
                    CommonPermission commonPermission = CommonPermission.from(repository.getPermission());
                    webHookConfigPanel.enableAdd(commonPermission.isSuper());
                    webHookConfigPanel.loadHooks(repository.getId(), WebHookSourceKind.HOOK_SOURCE_REPOSITORY);

                    break;
                case CONFIG_OPERATION:
                    if (repositoryOperationPanel == null) {
                        repositoryOperationPanel = new RepositoryOperationPanel();

                        repositoryOperationPanel.addCommonHandler(repositoryOperationPanelHandler);
                    }
                    root.add(repositoryOperationPanel);
                    root.setWidgetTopBottom(repositoryOperationPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
                    root.setWidgetLeftRight(repositoryOperationPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
                    repositoryOperationPanel.setData(repository);

                    break;
            }

        }
    }

    @Override
    public VwRepositoryEntity getData() {
        return repository;
    }

    @Override
    public void setData(VwRepositoryEntity obj) {
        repository = obj;
        toUI();
    }

    private void toUI() {
        list.selectFirst(true);
    }

    interface RepositorySettingPanelUiBinder extends UiBinder<DockLayoutPanel, RepositorySettingPanel> {
    }
}