package cn.mapway.gwt_template.client.project.group;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.server.config.AppConfig;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.Header;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.tree.Tree;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * 我的组织列表
 */
public class GroupList extends CommonEventComposite {
    interface GroupListUiBinder extends UiBinder<DockLayoutPanel, GroupList> {
    }

    private static GroupListUiBinder ourUiBinder = GWT.create(GroupListUiBinder.class);
    @UiField
    AiButton btnCreate;
    @UiField
    Header lbName;
    @UiField
    Label lbCount;
    @UiField
    Tree list;

    public GroupList() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @UiHandler("btnCreate")
    public void btnCreateClick(ClickEvent event) {
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        loadGroups();
    }

    private void loadGroups() {
        AppProxy.get()
    }

    @UiHandler("list")
    public void listCommon(CommonEvent event) {
    }
}