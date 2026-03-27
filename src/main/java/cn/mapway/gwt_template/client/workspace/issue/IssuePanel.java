package cn.mapway.gwt_template.client.workspace.issue;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.js.markdown.MarkdownConvert;
import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.client.widget.SmartEditor;
import cn.mapway.gwt_template.client.widget.file.CommonFileUploadResult;
import cn.mapway.gwt_template.client.widget.file.UploadData;
import cn.mapway.gwt_template.client.workspace.widget.EditableLabel;
import cn.mapway.gwt_template.client.workspace.widget.MarkdownBox;
import cn.mapway.gwt_template.client.workspace.widget.TaskPriorityDropdown;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectIssueCommentEntity;
import cn.mapway.gwt_template.shared.db.DevProjectIssueEntity;
import cn.mapway.gwt_template.shared.rpc.project.*;
import cn.mapway.gwt_template.shared.rpc.project.module.IssueCommentKind;
import cn.mapway.gwt_template.shared.rpc.project.module.IssueState;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectMember;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.tools.JSON;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.panel.MessagePanel;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import jsinterop.base.Js;

public class IssuePanel extends CommonEventComposite implements IData<DevProjectIssueEntity> {
    private static final IssuePanelUiBinder ourUiBinder = GWT.create(IssuePanelUiBinder.class);
    static int ADMIN_BAR_HEIGHT = 50;
    @UiField
    TaskPriorityDropdown ddlPriority;
    @UiField
    LayoutPanel root;
    @UiField
    DockLayoutPanel main;
    @UiField
    MessagePanel messagePanel;
    @UiField
    EditableLabel txtName;
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
    AiButton btnClose;
    @UiField
    ScrollPanel scroller;
    @UiField
    AiButton btnReopen;
    @UiField
    Label tip;
    @UiField
    HTMLPanel inputTools;
    @UiField
    SmartEditor editor;
    @UiField
    AiButton btnPublish;
    @UiField
    Image iconState;
    MarkdownConvert convert;
    private DevProjectIssueEntity issue;

    public IssuePanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
        ddlPriority.init(false);
        convert = new MarkdownConvert();
        assignTo.setEnabled(true);
        editor.appendTool(inputTools);
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

    private void toUI() {
        boolean renderData = updateUI();
        if (!renderData) {
            return;
        }

        txtName.setText(issue.getName());
        ddlPriority.setValue(issue.getPriority());
        markdownBox.setViewMode(true);
        markdownBox.setValue(issue.getSummary());


        //只有创建者 和负责人能够 进行评论和转移任务
        boolean isClosed = IssueState.fromCode(issue.getState()).equals(IssueState.IS_CLOSED);
        boolean isCharge = ClientContext.get().isCurrentUser(issue.getCharger());
        boolean isCreator = ClientContext.get().isCurrentUser(issue.getCreateUserId());
        tip.setText("");
        if (isClosed) {
            iconState.setResource(AppResource.INSTANCE.statusClosed());
            //关闭的项目　不允许变更
            root.setWidgetVisible(editor, false);
            txtName.setEditable(false);
            ddlPriority.setEnabled(false);
            markdownBox.setEnabled(false);
            main.setWidgetSize(saveBar, ADMIN_BAR_HEIGHT);
            if (isCreator) {
                btnReopen.setVisible(true);
                btnSave.setVisible(false);
            } else {
                btnReopen.setVisible(false);
                btnSave.setVisible(false);
            }
        } else {
            //打开的问题
            iconState.setResource(AppResource.INSTANCE.statusOpen());
            btnReopen.setVisible(false);
            if (isCreator) {
                main.setWidgetSize(saveBar, ADMIN_BAR_HEIGHT);
                btnSave.setVisible(true);
                txtName.setEditable(true);
                ddlPriority.setEnabled(true);
                markdownBox.setEnabled(true);

                root.setWidgetVisible(editor, true);
                assignTo.setAvatar(issue.getCreateAvatar(), issue.getChargeAvatar());

            } else if (isCharge) {
                main.setWidgetSize(saveBar, 0);
                root.setWidgetVisible(editor, true);
                txtName.setEditable(false);
                ddlPriority.setEnabled(false);
                markdownBox.setEnabled(false);
            } else {
                main.setWidgetSize(saveBar, 0);
                root.setWidgetVisible(editor, false);
                txtName.setEditable(false);
                ddlPriority.setEnabled(false);
                markdownBox.setEnabled(false);
            }
        }


        editor.setUploadUrl("/api/v1/project/upload");
        editor.clearUploadData().appendUploadData("path", AppConstant.UPLOAD_PREFIX_ISSUE_ATTACHMENT + issue.getId());
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
                adjustToDefaultSize(null);

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
                editor.setData("");
                adjustToDefaultSize(null);
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
        comment.setContent(editor.getData());
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

    @UiHandler("editor")
    public void editorCommon(CommonEvent event) {
        if (event.isOk()) {
            publishComment();
        } else if (event.isResize()) {
            Size size = event.getValue();
            int height = size.getYAsInt();
            adjustToDefaultSize(height);
        } else if (event.isUpload()) {
            String jsonData = event.getValue();
            CommonFileUploadResult parse = Js.uncheckedCast(JSON.parse(jsonData));
            UploadData result = parse.data;
            String data = editor.getData();
            String link = "";
            if (result.mime != null && result.mime.startsWith("image/")) {
                link = "\r\n![" + result.fileName + "](<" + result.relPath + ">)";
            } else {
                link = "\r\n[" + result.fileName + "](<" + result.relPath + ">)";
            }
            editor.setData(data + link);
        }
    }

    private void publishComment() {
        String comment = editor.getData();
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

    @UiHandler("btnPublish")
    public void btnPublishClick(ClickEvent event) {
        publishComment();
    }

    private void adjustToDefaultSize(Integer needHeight) {
        int DEFAULT_HEIGHT = 150;
        if (needHeight != null && needHeight > DEFAULT_HEIGHT) {
            DEFAULT_HEIGHT = needHeight;
        }
        Style style = scroller.getElement().getStyle();
        style.setPaddingBottom(DEFAULT_HEIGHT + 10, Style.Unit.PX);
        root.setWidgetBottomHeight(editor, 0, Style.Unit.PX, DEFAULT_HEIGHT, Style.Unit.PX);
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                scroller.scrollToBottom();
            }
        });
    }

    interface IssuePanelUiBinder extends UiBinder<LayoutPanel, IssuePanel> {
    }
}