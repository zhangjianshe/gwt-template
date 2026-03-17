package cn.mapway.gwt_template.client.workspace.res;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.resource.AppCss;
import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.client.workspace.task.ProjectMemberSelector;
import cn.mapway.gwt_template.shared.db.DevProjectResourceEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectMember;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectPermission;
import cn.mapway.gwt_template.shared.rpc.project.module.ResourceMember;
import cn.mapway.gwt_template.shared.rpc.project.res.*;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.AiTextBox;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

public class ResourceConfigPanel extends CommonEventComposite implements IData<DevProjectResourceEntity> {
    private static final ResourceConfigPanelUiBinder ourUiBinder = GWT.create(ResourceConfigPanelUiBinder.class);
    private static Popup<ResourceConfigPanel> popup;
    private final AppCss style = AppResource.INSTANCE.styles();
    @UiField
    AiTextBox txtColor;
    @UiField
    AiTextBox txtName;
    @UiField
    FlexTable memberTable;
    @UiField
    Label lbHeader;
    private DevProjectResourceEntity resource;

    public ResourceConfigPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
        txtColor.asColor();
        initTable();
    }

    public static Popup<ResourceConfigPanel> getDialog(boolean reuse) {
        if (reuse) {
            if (popup == null) {
                popup = createOne();
            }
            return popup;
        } else {
            return createOne();
        }
    }

    private static Popup<ResourceConfigPanel> createOne() {
        return new Popup<>(new ResourceConfigPanel());
    }

    private void initTable() {
        memberTable.addStyleName(style.table());
        memberTable.getRowFormatter().addStyleName(0, style.tableHeader());
        memberTable.setText(0, 0, "成员");
        memberTable.setText(0, 1, "权限");
        memberTable.setText(0, 2, "加入时间");
        Button btnAddUser = new Button("添加帐号");

        btnAddUser.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addUser(btnAddUser);
            }
        });
        memberTable.setWidget(0, 3, btnAddUser);
    }

    private void addUser(Widget widget) {
        Popup<ProjectMemberSelector> popup1 = ProjectMemberSelector.getPopup(true);
        popup1.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isSelect()) {
                    ProjectMember projectMember = event.getValue();
                    doAddResourceMember(projectMember);
                    popup1.hide();
                } else if (event.isClose()) {
                    popup1.hide();
                }
            }
        });
        popup1.getContent().setData(resource.getProjectId());
        popup1.showRelativeTo(widget);
    }

    private void doAddResourceMember(ProjectMember projectMember) {
        AddResourceMemberRequest request = new AddResourceMemberRequest();
        request.setResourceId(resource.getId());
        request.setPermission("");
        request.setUserId(projectMember.getUserId());
        AppProxy.get().addResourceMember(request, new AsyncCallback<RpcResult<AddResourceMemberResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<AddResourceMemberResponse> result) {
                if (result.isSuccess()) {
                    // reload
                    setData(resource);
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    @Override
    public DevProjectResourceEntity getData() {
        return resource;
    }

    @Override
    public void setData(DevProjectResourceEntity obj) {
        this.resource = obj;
        lbHeader.setText("资源目录配置: " + resource.getName());
        toUI();
    }

    private void toUI() {
        txtColor.setValue(resource.getColor());
        txtName.setValue(resource.getName());
        loadMember();
    }

    private void loadMember() {
        QueryResourceMemberRequest request = new QueryResourceMemberRequest();
        request.setResourceId(resource.getId());

        // 保留表头，清除数据行
        while (memberTable.getRowCount() > 1) {
            memberTable.removeRow(1);
        }

        AppProxy.get().queryResourceMember(request, new AsyncAdaptor<RpcResult<QueryResourceMemberResponse>>() {
            @Override
            public void onData(RpcResult<QueryResourceMemberResponse> result) {
                if (result.isSuccess()) {
                    renderMembers(result.getData().getMembers());
                }
            }
        });
    }

    private void renderMembers(java.util.List<ResourceMember> members) {
        for (ResourceMember m : members) {
            int row = memberTable.getRowCount();
            memberTable.getRowFormatter().addStyleName(row, style.tableRow());

            // 1. 用户信息 (使用 userBox 和 avatar 样式)
            HorizontalPanel userBox = new HorizontalPanel();
            userBox.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
            userBox.addStyleName(style.userBox());

            Image avatar = new Image(m.getAvatar());
            avatar.addStyleName(style.avatar());
            userBox.add(avatar);

            Label name = new Label(m.getNickName() + " (" + m.getUserName() + ")");
            name.addStyleName(style.userName());
            userBox.add(name);

            memberTable.setWidget(row, 0, userBox);

            // 2. 权限展示
            ProjectPermission p = ProjectPermission.from(m.getPermission());
            String roleText = p.isOwner() ? "所有者" : (p.isAdmin() ? "管理员" : "成员");
            Label lbRole = new Label(roleText);
            if (p.isOwner()) lbRole.addStyleName(style.success());
            memberTable.setWidget(row, 1, lbRole);

            // 3. 时间
            memberTable.setText(row, 2, m.getCreateTime() == null ? "-" : m.getCreateTime().toString());

            // 4. 操作按钮 (移除成员)
            Anchor lnkDelete = new Anchor("移除");
            lnkDelete.addStyleName(style.primaryText());
            lnkDelete.addClickHandler(event -> {
                if (Window.confirm("确定要移除成员 " + m.getNickName() + " 吗?")) {
                    doDeleteMember(m);
                }
            });

            // 如果是所有者，通常不允许随便移除自己（除非有多个所有者），这里简单逻辑
            if (p.isOwner()) {
                lnkDelete.addStyleName(style.menuItemDisabled());
            }

            memberTable.setWidget(row, 3, lnkDelete);
        }
    }

    private void doDeleteMember(ResourceMember m) {
        DeleteResourceMemberRequest req = new DeleteResourceMemberRequest();
        req.setResourceId(resource.getId());
        req.setUserId(m.getUserId());

        AppProxy.get().deleteResourceMember(req, new AsyncCallback<RpcResult<DeleteResourceMemberResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<DeleteResourceMemberResponse> result) {
                if (result.isSuccess()) {
                    loadMember(); // 重新加载
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    interface ResourceConfigPanelUiBinder extends UiBinder<DockLayoutPanel, ResourceConfigPanel> {
    }
}