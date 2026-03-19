package cn.mapway.gwt_template.client.repository.member;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.widget.Head;
import cn.mapway.gwt_template.shared.db.VwRepositoryMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.repository.*;
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
    private String repositoryId;
    private final CommonEventHandler itemHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent event) {
            if (event.isDelete()) {
                VwRepositoryMemberEntity member = event.getValue();
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

    private void confirmdelete(VwRepositoryMemberEntity member) {
        String message = "移除项目组成员" + member.getUserName() + "?";
        ClientContext.get().confirmDelete(message).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
            @Override
            public IThenable<Object> onInvoke(Void p0) {
                doDelete(member);
                return null;
            }
        });
    }

    private void doDelete(VwRepositoryMemberEntity member) {
        DeleteRepositoryMemberRequest request = new DeleteRepositoryMemberRequest();
        request.setRepositoryId(member.getRepositoryId());
        request.setUserId(member.getUserId());
        AppProxy.get().deleteRepositoryMember(request, new AsyncCallback<RpcResult<DeleteRepositoryMemberResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<DeleteRepositoryMemberResponse> result) {
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
        return repositoryId;
    }

    @Override
    public void setData(String obj) {
        repositoryId = obj;
        loadMembers();
    }

    /**
     * 加载项目组成员
     */
    private void loadMembers() {
        list.clear();
        lbHeader.setText("项目组成员");
        QueryRepositoryMemberRequest request = new QueryRepositoryMemberRequest();
        request.setRepositoryId(repositoryId);
        AppProxy.get().queryRepositoryMember(request, new AsyncCallback<RpcResult<QueryRepositoryMemberResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryRepositoryMemberResponse> result) {
                if (result.isSuccess()) {
                    CommonPermission commonPermission = CommonPermission.from(result.getData().getCurrentUserPermission());
                    renderMember(commonPermission.isSuper(), result.getData().getMembers());
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    private void renderMember(boolean adminer, List<VwRepositoryMemberEntity> members) {
        btnAdd.setEnabled(adminer);
        if (members == null || members.isEmpty()) {
            list.clear();
            lbHeader.setText("项目组成员(0)");
            list.add(new MessagePanel().setText("没有成员"));
            return;
        }
        for (VwRepositoryMemberEntity member : members) {
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

        UpdateRepositoryMemberRequest request = new UpdateRepositoryMemberRequest();
        request.setPermission(CommonPermission.empty().setRead(true).toString());
        request.setUserId(userId);
        request.setRepositoryId(repositoryId);
        AppProxy.get().updateRepositoryMember(request, new AsyncCallback<RpcResult<UpdateRepositoryMemberResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateRepositoryMemberResponse> result) {
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