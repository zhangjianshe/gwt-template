package cn.mapway.gwt_template.client.project.group;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.shared.db.DevGroupEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevGroupRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevGroupResponse;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.Header;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.tree.Tree;
import cn.mapway.ui.client.widget.tree.TreeItem;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;

import java.util.List;

/**
 * 我的组织列表
 */
public class GroupList extends CommonEventComposite {
    private static final GroupListUiBinder ourUiBinder = GWT.create(GroupListUiBinder.class);
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
        AppProxy.get().queryDevGroup(new QueryDevGroupRequest(), new AsyncAdaptor<RpcResult<QueryDevGroupResponse>>() {
            @Override
            public void onData(RpcResult<QueryDevGroupResponse> result) {
                renderGroup(result.getData().getGroups());
            }
        });
    }

    private void renderGroup(List<DevGroupEntity> groups) {
        list.clear();
        if (groups == null || groups.isEmpty()) {
            list.setMessage("没有键入或者创建组织", 60);
            return;
        }
        list.setMessage("", 0);
        for (DevGroupEntity group : groups) {
            TreeItem item = list.addItem(null, group.getFullName() + "(" + group.getName() + ")");
            item.setData(group);
            item.appendRightWidget(new Label(String.valueOf(group.getMemberCount())), null);
        }
    }

    @UiHandler("list")
    public void listCommon(CommonEvent event) {
    }

    interface GroupListUiBinder extends UiBinder<DockLayoutPanel, GroupList> {
    }
}