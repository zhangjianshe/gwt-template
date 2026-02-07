package cn.mapway.gwt_template.client.project.member;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.widget.Head;
import cn.mapway.gwt_template.shared.db.VwProjectMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.*;
import cn.mapway.gwt_template.shared.rpc.user.CommonPermission;
import cn.mapway.ui.client.IUserInfo;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.PlusButton;
import cn.mapway.ui.client.widget.panel.MessagePanel;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import elemental2.core.JsArray;
import elemental2.promise.IThenable;

import java.util.List;

/**
 * 项目组成员
 */
public class MemberList extends CommonEventComposite implements IData<String> {
    private static final MemberListUiBinder ourUiBinder = GWT.create(MemberListUiBinder.class);
    @UiField
    VerticalPanel list;
    @UiField
    PlusButton btnAdd;
    @UiField
    Label lbMessage;
    private final MouseOverHandler hoverHandler = new MouseOverHandler() {
        @Override
        public void onMouseOver(MouseOverEvent event) {
            Widget widget = (Widget) event.getSource();
            String title = widget.getTitle();
            lbMessage.setText(title);
        }
    };
    @UiField
    HorizontalPanel buttons;
    @UiField
    Head lbHeader;
    private String projectId;
    private final CommonEventHandler itemHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent event) {
            if (event.isDelete()) {
                VwProjectMemberEntity member = event.getValue();
                confirmdelete(member);
            }
        }
    };

    public MemberList() {
        initWidget(ourUiBinder.createAndBindUi(this));

        for (int i = 0; i < buttons.getWidgetCount(); i++) {
            Widget widget = buttons.getWidget(i);
            widget.addDomHandler(hoverHandler, MouseOverEvent.getType());
        }
    }

    private void confirmdelete(VwProjectMemberEntity member) {
        String message = "移除项目组成员" + member.getUserName() + "?";
        ClientContext.get().confirmDelete(message).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
            @Override
            public IThenable<Object> onInvoke(Void p0) {
                doDelete(member);
                return null;
            }
        });
    }

    private void doDelete(VwProjectMemberEntity member) {
        DeleteProjectMemberRequest request = new DeleteProjectMemberRequest();
        request.setProjectId(member.getProjectId());
        request.setUserId(member.getUserId());
        AppProxy.get().deleteProjectMember(request, new AsyncCallback<RpcResult<DeleteProjectMemberResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<DeleteProjectMemberResponse> result) {
                if (result.isSuccess()) {
                    loadMembers();
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    @Override
    public String getData() {
        return projectId;
    }

    @Override
    public void setData(String obj) {
        projectId = obj;
        loadMembers();
    }

    /**
     * 加载项目组成员
     */
    private void loadMembers() {
        list.clear();
        lbHeader.setText("项目组成员");
        QueryProjectMemberRequest request = new QueryProjectMemberRequest();
        request.setProjectId(projectId);
        AppProxy.get().queryProjectMember(request, new AsyncCallback<RpcResult<QueryProjectMemberResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryProjectMemberResponse> result) {
                if (result.isSuccess()) {
                    CommonPermission commonPermission = CommonPermission.fromPermission(result.getData().getCurrentUserPermission());
                    renderMember(commonPermission.isAdmin(), result.getData().getMembers());
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    private void renderMember(boolean adminer, List<VwProjectMemberEntity> members) {
        btnAdd.setEnabled(adminer);
        if (members == null || members.isEmpty()) {
            list.clear();
            lbHeader.setText("项目组成员(0)");
            list.add(new MessagePanel().setText("没有成员"));
            return;
        }
        for (VwProjectMemberEntity member : members) {
            MemberItem memberItem = new MemberItem();
            memberItem.setAdmin(adminer);
            memberItem.setData(member);
            memberItem.addCommonHandler(itemHandler);
            list.add(memberItem);
        }
        lbHeader.setText("项目组成员(" + members.size() + ")");

    }

    @UiHandler("btnAdd")
    public void btnAddClick(ClickEvent event) {
        ClientContext.get().chooseUser().then(new IThenable.ThenOnFulfilledCallbackFn<JsArray<IUserInfo>, Object>() {
            @Override
            public IThenable<Object> onInvoke(JsArray<IUserInfo> p0) {
                doAddUser(p0);
                return null;
            }
        });
    }

    private void doAddUser(JsArray<IUserInfo> p0) {
        if (p0 == null || p0.length == 0) {
            return;
        }
        IUserInfo info = p0.at(0);
        Long userId = -1L;
        try {
            userId = Long.parseLong(info.getId());
        } catch (NumberFormatException e) {
            ClientContext.get().toast(0, 0, "用户ID不能解析");
            return;
        }

        UpdateProjectMemberRequest request = new UpdateProjectMemberRequest();
        request.setPermission(CommonPermission.fromPermission(0).setRead(true).getPermission());
        request.setUserId(userId);
        request.setProjectId(projectId);
        AppProxy.get().updateProjectMember(request, new AsyncCallback<RpcResult<UpdateProjectMemberResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateProjectMemberResponse> result) {
                if (result.isSuccess()) {
                    loadMembers();
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    interface MemberListUiBinder extends UiBinder<HTMLPanel, MemberList> {
    }
}