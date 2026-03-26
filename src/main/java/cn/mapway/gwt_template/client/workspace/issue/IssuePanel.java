package cn.mapway.gwt_template.client.workspace.issue;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.js.markdown.MarkdownConvert;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.client.workspace.widget.EditableLabel;
import cn.mapway.gwt_template.client.workspace.widget.TaskPriorityDropdown;
import cn.mapway.gwt_template.shared.db.DevProjectIssueCommentEntity;
import cn.mapway.gwt_template.shared.db.DevProjectIssueEntity;
import cn.mapway.gwt_template.shared.rpc.project.*;
import cn.mapway.gwt_template.shared.rpc.project.module.IssueCommentKind;
import cn.mapway.gwt_template.shared.rpc.project.module.IssueState;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectMember;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.panel.MessagePanel;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

public class IssuePanel extends CommonEventComposite implements IData<DevProjectIssueEntity> {
    private static final IssuePanelUiBinder ourUiBinder = GWT.create(IssuePanelUiBinder.class);
    @UiField
    TaskPriorityDropdown ddlPriority;
    @UiField
    IssueStateDropdown ddlState;
    @UiField
    LayoutPanel root;
    @UiField
    DockLayoutPanel main;
    @UiField
    MessagePanel messagePanel;
    @UiField
    EditableLabel txtName;
    @UiField
    AssignUserPanel charger;
    @UiField
    HTMLPanel saveBar;
    @UiField
    AiButton btnSave;
    @UiField
    HTMLPanel commentContainer;
    @UiField
    MarkdownBox markdownBox;
    @UiField
    AssignUserPanel assignTo;
    @UiField
    TextArea txtComment;
    @UiField
    AiButton btnClose;
    @UiField
    AiButton btnComment;
    @UiField
    VerticalPanel commentPanel;
    @UiField
    ScrollPanel scroller;
    @UiField
    AiButton btnReopen;
    @UiField
    Label tip;
    MarkdownConvert convert;
    private DevProjectIssueEntity issue;

    public IssuePanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
        ddlPriority.init(false);
        ddlState.init(false);
        convert = new MarkdownConvert();
        assignTo.setEnabled(true);
    }

    @Override
    public DevProjectIssueEntity getData() {
        return issue;
    }

    @Override
    public void setData(DevProjectIssueEntity devProjectIssueEntity) {
        issue = devProjectIssueEntity;
        toUI();
    }

    boolean canEdit() {
        if (issue == null) {
            return false;
        }
        return ClientContext.get().isCurrentUser(issue.getCreateUserId());
    }

    private void toUI() {
        boolean renderData = updateUI();
        if (!renderData) {
            return;
        }

        txtName.setText(issue.getName());
        ddlPriority.setValue(issue.getPriority());
        ddlState.setValue(issue.getState());
        charger.setProjectId(issue.getProjectId());
        charger.setAvatar(issue.getCreateAvatar(), issue.getChargeAvatar());
        markdownBox.setViewMode(true);
        markdownBox.setValue(issue.getSummary());


        //只有创建者 和负责人能够 进行评论和转移任务
        boolean isClosed = IssueState.fromCode(issue.getState()).equals(IssueState.IS_CLOSED);
        boolean isCharge = ClientContext.get().isCurrentUser(issue.getCharger());
        boolean isCreator = ClientContext.get().isCurrentUser(issue.getCreateUserId());
        tip.setText("");
        if (isClosed) {
            //关闭的项目　不允许变更
            main.setWidgetSize(commentPanel, 0);
            txtName.setEditable(false);
            ddlPriority.setEnabled(false);
            ddlState.setEnabled(false);
            charger.setEnabled(false);
            markdownBox.setEnabled(false);
            main.setWidgetSize(saveBar, 50);
            if (isCreator) {
                btnReopen.setVisible(true);
                btnSave.setVisible(false);
            } else {
                btnReopen.setVisible(false);
                btnSave.setVisible(false);
            }
        } else {
            //打开的问题

            btnReopen.setVisible(false);
            if (isCreator) {
                main.setWidgetSize(saveBar, 50);
                btnSave.setVisible(true);
                txtName.setEditable(true);
                ddlPriority.setEnabled(true);
                ddlState.setEnabled(false);
                charger.setEnabled(true);
                markdownBox.setEnabled(true);
                tip.setText("点击开始编辑 Ctrl+s 退出编辑");

                main.setWidgetSize(commentPanel, 150);
                assignTo.setAvatar(issue.getCreateAvatar(), issue.getChargeAvatar());

            } else if (isCharge) {
                main.setWidgetSize(saveBar, 0);
                main.setWidgetSize(commentPanel, 150);
                txtName.setEditable(false);
                ddlPriority.setEnabled(false);
                ddlState.setEnabled(false);
                charger.setEnabled(false);
                markdownBox.setEnabled(false);
            } else {
                main.setWidgetSize(saveBar, 0);
                main.setWidgetSize(commentPanel, 0);
                txtName.setEditable(false);
                ddlPriority.setEnabled(false);
                ddlState.setEnabled(false);
                charger.setEnabled(false);
                markdownBox.setEnabled(false);
            }
        }


        assignTo.setProjectId(issue.getProjectId());
        loadComments(issue.getId());
    }

    private void loadComments(String issueId) {
        QueryProjectIssueCommentRequest request = new QueryProjectIssueCommentRequest();
        request.setIssueId(issueId);
        AppProxy.get().queryProjectIssueComment(request, new AsyncAdaptor<RpcResult<QueryProjectIssueCommentResponse>>() {
            @Override
            public void onData(RpcResult<QueryProjectIssueCommentResponse> result) {
                commentContainer.clear();
                for (DevProjectIssueCommentEntity comment : result.getData().getComments()) {
                    CommentItem commentItem = new CommentItem();
                    commentItem.setData(comment);
                    commentContainer.add(commentItem);
                }
                if (result.getData().getComments().isEmpty()) {
                    MessagePanel messagePanel = new MessagePanel();
                    messagePanel.setText("还没有评论");
                    messagePanel.setHeight("300px");
                    commentContainer.add(messagePanel);
                }
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        scroller.scrollToBottom();
                    }
                });
            }
        });
    }

    private boolean updateUI() {
        if (issue == null) {
            root.setWidgetVisible(main, false);
            root.setWidgetVisible(messagePanel, true);
            messagePanel.setText("问题预览");
            return false;
        } else {
            root.setWidgetVisible(main, true);
            root.setWidgetVisible(messagePanel, false);
            return true;
        }
    }

    @UiHandler("charger")
    public void chargerCommon(CommonEvent event) {
        if (event.isSelect()) {
            ProjectMember projectMember = event.getValue();
            assignToUser(projectMember);
        }
    }

    @UiHandler("btnSave")
    public void btnSaveClick(ClickEvent event) {
        if (issue == null) {
            return;
        }
        issue.setPriority((Integer) ddlPriority.getValue());
        issue.setName(txtName.getValue());
        issue.setSummary(markdownBox.getValue());
        UpdateProjectIssueRequest request = new UpdateProjectIssueRequest();
        request.setIssue(issue);
        AppProxy.get().updateProjectIssue(request, new AsyncCallback<RpcResult<UpdateProjectIssueResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateProjectIssueResponse> result) {
                if (result.isSuccess()) {
                    fireEvent(CommonEvent.updateEvent(result.getData().getIssue()));
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    @UiHandler("markdownBox")
    public void markdownBoxCommon(CommonEvent event) {
    }

    @UiHandler("btnComment")
    public void btnCommentClick(ClickEvent event) {
        //发表评论
        String comment = txtComment.getValue();
        if (StringUtil.isBlank(comment)) {
            ClientContext.get().confirm("请输入评论内容");
            return;
        }
        DevProjectIssueCommentEntity commentEntity = new DevProjectIssueCommentEntity();
        commentEntity.setKind(IssueCommentKind.ICK_COMMENT.getCode());
        commentEntity.setContent(comment);
        commentEntity.setIssueId(issue.getId());
        createComment(commentEntity);
    }

    private void createComment(DevProjectIssueCommentEntity commentEntity) {
        UpdateProjectIssueCommentRequest request = new UpdateProjectIssueCommentRequest();
        request.setComment(commentEntity);
        AppProxy.get().updateProjectIssueComment(request, new AsyncAdaptor<RpcResult<UpdateProjectIssueCommentResponse>>() {
            @Override
            public void onData(RpcResult<UpdateProjectIssueCommentResponse> result) {
                if (result.getData().getUpdateIssue()) {
                    reloadIssue();
                } else {
                    loadComments(issue.getId());
                }
                txtComment.setValue("");
            }
        });
    }

    private void reloadIssue() {
        QueryProjectIssueRequest request = new QueryProjectIssueRequest();
        request.setIssueId(issue.getId());
        AppProxy.get().queryProjectIssue(request, new AsyncAdaptor<RpcResult<QueryProjectIssueResponse>>() {
            @Override
            public void onData(RpcResult<QueryProjectIssueResponse> result) {
                setData(result.getData().getIssues().get(0));
                fireEvent(CommonEvent.updateEvent(result.getData().getIssues().get(0)));
            }
        });
    }

    @UiHandler("btnClose")
    public void btnCloseClick(ClickEvent event) {
        DevProjectIssueCommentEntity comment = new DevProjectIssueCommentEntity();
        comment.setContent(txtComment.getValue());
        comment.setKind(IssueCommentKind.ICK_CLOSE.getCode());
        comment.setIssueId(issue.getId());
        createComment(comment);
    }

    @UiHandler("assignTo")
    public void assignToCommon(CommonEvent event) {
        if (event.isSelect()) {
            ProjectMember projectMember = event.getValue();
          assignToUser(projectMember);
        }
    }

    private void assignToUser(ProjectMember projectMember) {
        DevProjectIssueCommentEntity comment = new DevProjectIssueCommentEntity();
        comment.setContent(projectMember.getUserId() + "");
        comment.setKind(IssueCommentKind.ICK_REASSIGN.getCode());
        comment.setIssueId(issue.getId());
        createComment(comment);
    }

    @UiHandler("btnReopen")
    public void btnReopenClick(ClickEvent event) {
        DevProjectIssueCommentEntity comment = new DevProjectIssueCommentEntity();
        comment.setContent("");
        comment.setKind(IssueCommentKind.ICK_REPOEN.getCode());
        comment.setIssueId(issue.getId());
        createComment(comment);
    }

    interface IssuePanelUiBinder extends UiBinder<LayoutPanel, IssuePanel> {
    }
}