package cn.mapway.gwt_template.client.workspace.task;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.js.markdown.MarkdownConvert;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.client.widget.SmartEditor;
import cn.mapway.gwt_template.client.widget.Uploader;
import cn.mapway.gwt_template.client.widget.file.CommonFileUploadResult;
import cn.mapway.gwt_template.client.widget.file.UploadData;
import cn.mapway.gwt_template.client.workspace.issue.AssignUserPanel;
import cn.mapway.gwt_template.client.workspace.widget.EditableLabel;
import cn.mapway.gwt_template.client.workspace.widget.MarkdownBox;
import cn.mapway.gwt_template.client.workspace.widget.ProgressSelector;
import cn.mapway.gwt_template.client.workspace.widget.TaskPriorityDropdown;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectTaskCommentEntity;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.*;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskKind;
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

/**
 * 甘特图任务的comment面板
 */
public class TaskCommentPanel extends CommonEventComposite implements IData<DevProjectTaskEntity> {
    private static final TaskCommentPanelUiBinder ourUiBinder = GWT.create(TaskCommentPanelUiBinder.class);
    static int ADMIN_BAR_HEIGHT = 50;
    @UiField
    TaskPriorityDropdown ddlPriority;
    @UiField
    LayoutPanel root;
    @UiField
    SplitLayoutPanel main;
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
    ScrollPanel scroller;
    @UiField
    Label tip;
    @UiField
    HTMLPanel inputTools;
    @UiField
    SmartEditor editor;
    @UiField
    AiButton btnPublish;
    @UiField
    Label iconState;
    @UiField
    Uploader btnUploader;
    @UiField
    DockLayoutPanel top;
    @UiField
    AssignUserPanel assignPanel;
    @UiField
    ProgressSelector progressSelector;
    MarkdownConvert convert;
    CommonPermission currentUserPermission;
    private DevProjectTaskEntity taskEntity;

    public TaskCommentPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
        ddlPriority.init(false);
        convert = new MarkdownConvert();
        editor.appendTool(inputTools);
        progressSelector.setThemeColor("brown");
    }

    @Override
    public DevProjectTaskEntity getData() {
        return taskEntity;
    }

    @Override
    public void setData(DevProjectTaskEntity task) {
        taskEntity = task;
        toUI();
    }

    private void toUI() {
        boolean renderData = updateUI();
        if (!renderData) {
            return;
        }

        txtName.setText(taskEntity.getName());
        ddlPriority.setValue(taskEntity.getPriority());
        markdownBox.setViewMode(true);
        markdownBox.setValue(taskEntity.getSummary());
        progressSelector.setProgress(taskEntity.getProgress());


        tip.setText("");
        DevTaskKind kind = DevTaskKind.fromCode(taskEntity.getKind());
        iconState.setText(kind.getUnicode());
        iconState.getElement().getStyle().setColor(kind.getColor());

        //只有创建者 和负责人能够 进行评论和转移任务
        boolean isCharge = ClientContext.get().isCurrentUser(taskEntity.getCharger());
        boolean isCreator = ClientContext.get().isCurrentUser(taskEntity.getCreateUserId());

        assignPanel.setProjectId(taskEntity.getProjectId());
        assignPanel.setAvatar(taskEntity.getCreateAvatar(), taskEntity.getChargeAvatar());

        if (isCreator || currentUserPermission.isSuper()) {
            top.setWidgetSize(saveBar, ADMIN_BAR_HEIGHT);
            btnSave.setVisible(true);
            root.setWidgetVisible(editor, true);
            btnUploader.setVisible(true);
            txtName.setEditable(true);
            ddlPriority.setEnabled(true);
            markdownBox.setEnabled(true);
            assignPanel.setVisible(true);
            progressSelector.setEnabled(true);
            root.setWidgetVisible(editor, true);
        } else if (isCharge) {
            top.setWidgetSize(saveBar, ADMIN_BAR_HEIGHT);
            root.setWidgetVisible(editor, true);
            txtName.setEditable(false);
            btnUploader.setVisible(true);
            ddlPriority.setEnabled(false);
            markdownBox.setEnabled(true);
            progressSelector.setEnabled(true);
            assignPanel.setVisible(false);
        } else {
            top.setWidgetSize(saveBar, 0);
            root.setWidgetVisible(editor, true);
            txtName.setEditable(false);
            ddlPriority.setEnabled(false);
            markdownBox.setEnabled(false);
            assignPanel.setVisible(false);
            btnUploader.setVisible(false);
            progressSelector.setEnabled(false);
        }

        String path = AppConstant.UPLOAD_PREFIX_TASK_COMMENT + taskEntity.getId();
        btnUploader.setActionAndPath(AppConstant.DEFAULT_UPLOAD_LOCATION, path);
        editor.setActionAndPath(AppConstant.DEFAULT_UPLOAD_LOCATION, path);
        loadComments(taskEntity.getId());
    }

    private void loadComments(String taskId) {
        QueryProjectTaskCommentRequest request = new QueryProjectTaskCommentRequest();
        request.setTaskId(taskId);
        AppProxy.get().queryProjectTaskComment(request, new AsyncAdaptor<RpcResult<QueryProjectTaskCommentResponse>>() {
            @Override
            public void onData(RpcResult<QueryProjectTaskCommentResponse> result) {
                commentContainer.clear();
                for (DevProjectTaskCommentEntity comment : result.getData().getComments()) {
                    TaskCommentItem commentItem = new TaskCommentItem();
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
        if (taskEntity == null) {
            root.setWidgetVisible(main, false);
            root.setWidgetVisible(messagePanel, true);
            messagePanel.setText("任务信息");
            return false;
        } else {
            root.setWidgetVisible(main, true);
            root.setWidgetVisible(messagePanel, false);
            return true;
        }
    }

    @UiHandler("btnSave")
    public void btnSaveClick(ClickEvent event) {
        if (taskEntity == null) {
            return;
        }
        taskEntity.setPriority((Integer) ddlPriority.getValue());
        taskEntity.setName(txtName.getValue());
        taskEntity.setSummary(markdownBox.getValue());
        taskEntity.setProgress(progressSelector.getProgress());
        UpdateProjectTaskRequest request = new UpdateProjectTaskRequest();
        request.setProjectTask(taskEntity);
        AppProxy.get().updateProjectTask(request, new AsyncCallback<RpcResult<UpdateProjectTaskResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateProjectTaskResponse> result) {
                if (result.isSuccess()) {
                    fireEvent(CommonEvent.updateEvent(result.getData().getProjectTask()));
                    ClientContext.get().toast(0, 0, "更新成功");
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    @UiHandler("markdownBox")
    public void markdownBoxCommon(CommonEvent event) {
    }

    private void createComment(DevProjectTaskCommentEntity commentEntity) {
        UpdateProjectTaskCommentRequest request = new UpdateProjectTaskCommentRequest();
        request.setComment(commentEntity);
        AppProxy.get().updateProjectTaskComment(request, new AsyncAdaptor<RpcResult<UpdateProjectTaskCommentResponse>>() {
            @Override
            public void onData(RpcResult<UpdateProjectTaskCommentResponse> result) {
                loadComments(taskEntity.getId());
                editor.setData("");
                adjustToDefaultSize(null);
            }
        });
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
        DevProjectTaskCommentEntity commentEntity = new DevProjectTaskCommentEntity();
        commentEntity.setTaskId(taskEntity.getId());
        commentEntity.setContent(comment);
        commentEntity.setParentId("");
        createComment(commentEntity);
    }

    @UiHandler("btnPublish")
    public void btnPublishClick(ClickEvent event) {
        publishComment();
    }

    @UiHandler("btnUploader")
    public void btnUploaderCommon(CommonEvent event) {
        if (event.isUpload()) {
            UploadData result = Js.uncheckedCast(event.getValue());
            String data = markdownBox.getValue();
            String link = "";
            if (result.mime != null && result.mime.startsWith("image/")) {
                link = "\n\n![" + result.fileName + "](<" + result.relPath + ">)";
            } else {
                link = "\n\n[" + result.fileName + "](<" + result.relPath + ">)";
            }
            markdownBox.setValue(data + link);
        }
    }

    @UiHandler("assignPanel")
    public void assignPanelCommon(CommonEvent event) {
        if (event.isSelect()) {
            ProjectMember member = event.getValue();
            taskEntity.setCharger(member.getUserId());
        }
    }

    @UiHandler("progressSelector")
    public void progressSelectorCommon(CommonEvent event) {
        if (event.isValueChanged()) {
            int progress = event.getValue();
            taskEntity.setProgress(progress);
            fireEvent(CommonEvent.progressEvent(taskEntity));
        }
    }

    private void adjustToDefaultSize(Integer needHeight) {
        int DEFAULT_HEIGHT = 150;
        if (needHeight != null && needHeight > DEFAULT_HEIGHT) {
            DEFAULT_HEIGHT = needHeight;
        }
        Style style = scroller.getElement().getStyle();
        style.setPaddingBottom(DEFAULT_HEIGHT + 10, Style.Unit.PX);
        root.setWidgetBottomHeight(editor, 0, Style.Unit.PX, DEFAULT_HEIGHT, Style.Unit.PX);

        // 使用多次延迟确保在图片加载和布局完成后滚动
        scheduleScrollToBottom();
    }

    private void scheduleScrollToBottom() {
        // 第一次尝试：立即排期
        Scheduler.get().scheduleDeferred(() -> scroller.scrollToBottom());

        // 第二次尝试：稍作延迟，处理图片异步撑开高度的情况
        new com.google.gwt.user.client.Timer() {
            @Override
            public void run() {
                scroller.scrollToBottom();
            }
        }.schedule(300); // 300ms 通常足以让浏览器计算出新加入图片的初始布局
    }

    public void setUserPermission(CommonPermission currentUserPermission) {
        this.currentUserPermission = currentUserPermission;
    }

    interface TaskCommentPanelUiBinder extends UiBinder<LayoutPanel, TaskCommentPanel> {
    }
}