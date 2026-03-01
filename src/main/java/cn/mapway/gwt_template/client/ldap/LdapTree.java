package cn.mapway.gwt_template.client.ldap;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.shared.rpc.ldap.*;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.SearchBox;
import cn.mapway.ui.client.widget.tree.Tree;
import cn.mapway.ui.client.widget.tree.TreeItem;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;

/**
 * LDAP树
 */
public class LdapTree extends CommonEventComposite {
    private static final LdapTreeUiBinder ourUiBinder = GWT.create(LdapTreeUiBinder.class);
    @UiField
    SearchBox searchBox;
    @UiField
    Tree ldapTree;

    public LdapTree() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public void loadRoot() {
        ldapTree.clear();
        ldapTree.clearMessage();
        ldapTree.setMessage("查询LDAP服务器信息...", 120);
        AppProxy.get().queryLdapRootDse(new QueryLdapRootDseRequest(), new AsyncAdaptor<RpcResult<QueryLdapRootDseResponse>>() {
            @Override
            public void onData(RpcResult<QueryLdapRootDseResponse> result) {
                renderRoot(result.getData());
            }
        });
    }

    private void renderRoot(QueryLdapRootDseResponse data) {
        ldapTree.clear();
        RootDse rootDse = data.getRootDse();
        if (rootDse.getNamingContexts() == null || rootDse.getNamingContexts().isEmpty()) {
            ldapTree.setMessage("没有返回LDAP服务器信息", 120);
            return;
        }
        ldapTree.setMessage("", 0);
        TreeItem item = ldapTree.addItem(null, rootDse.getNamingContexts().get(0), Fonts.LDAP);
        item.setData(rootDse);
    }

    @UiHandler("ldapTree")
    public void ldapTreeCommon(CommonEvent event) {
        if (event.isSelect()) {
            TreeItem treeItem = event.getValue();
            Object data = treeItem.getData();
            if (data instanceof RootDse) {
                if (treeItem.getChildren().isEmpty()) {
                    RootDse rootDse = (RootDse) data;
                    loadSubNode(treeItem, rootDse.getNamingContexts().get(0));
                }
            } else if (data instanceof LdapNodeData) {
                LdapNodeData nodeData = (LdapNodeData) data;
                if (nodeData.isFolder() && treeItem.getChildren().isEmpty()) {
                    loadSubNode(treeItem, nodeData.getDn());
                }
                fireEvent(CommonEvent.selectEvent(nodeData));
            }
        }
    }

    private void loadSubNode(TreeItem parentItem, String dn) {
        QueryLdapNodeDataRequest request = new QueryLdapNodeDataRequest();
        request.setDn(dn);
        AppProxy.get().queryLdapNodeData(request, new AsyncAdaptor<RpcResult<QueryLdapNodeDataResponse>>() {
            @Override
            public void onData(RpcResult<QueryLdapNodeDataResponse> result) {
                parentItem.clear();
                for (LdapNodeData nodeData : result.getData().getNodes()) {
                    String mainType = nodeData.getStructuralObjectClass();
                    String icon = Fonts.PROPERTY;
                    if ("organizationalUnit".equalsIgnoreCase(mainType)) {
                        icon = Fonts.DEPARTMENT;
                    } else if ("inetOrgPerson".equalsIgnoreCase(mainType)) {
                        icon = Fonts.ACCOUNT;
                    } else if ("groupOfNames".equalsIgnoreCase(mainType)) {
                        icon = Fonts.GROUP;
                    }
                    TreeItem item = ldapTree.addItem(parentItem, nodeData.getName(), icon);
                    item.setData(nodeData);
                }
            }
        });
    }

    interface LdapTreeUiBinder extends UiBinder<DockLayoutPanel, LdapTree> {
    }
}