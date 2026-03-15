package cn.mapway.gwt_template.client.workspace.task;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.workspace.widget.TaskKindDropdown;
import cn.mapway.gwt_template.client.workspace.widget.TaskPriorityDropdown;
import cn.mapway.gwt_template.client.workspace.widget.TaskStatusDropdown;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskKind;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectMember;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import lombok.Setter;

/**
 * 任务编辑器
 */
public class DevTaskEditor extends CommonEventComposite implements RequiresResize, IData<DevProjectTaskEntity> {
    private static final DevTaskEditorUiBinder ourUiBinder = GWT.create(DevTaskEditorUiBinder.class);
    private static Popup<DevTaskEditor> dialog;
    @UiField
    TextBox txtName;
    @UiField
    ProjectMemberWidget member;
    @UiField
    TaskKindDropdown ddlKind;
    @UiField
    DivElement back;
    @UiField
    HTMLPanel editor;
    @UiField
    AiButton btnSave;
    @UiField
    AiButton btnClose;
    @UiField
    DockLayoutPanel root;
    @UiField
    TextArea txtSummary;
    @UiField
    TaskStatusDropdown ddlStatus;
    boolean initialize = false;
    @UiField
    TaskPriorityDropdown ddlPriority;
    @Setter
    Callback<Void, Void> afterSave = null;
    private DevProjectTaskEntity task;

    public DevTaskEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));

        ddlKind.addValueChangeHandler(event -> {
            DevTaskKind kind = DevTaskKind.fromCode((Integer) event.getValue());
            updateBackground(kind);
            updateButtons(kind);
        });
    }

    public static Popup<DevTaskEditor> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Popup<DevTaskEditor> createOne() {

        final Popup<DevTaskEditor> widgets = new Popup<>(new DevTaskEditor()) {
            @Override
            protected void onPreviewNativeEvent(Event.NativePreviewEvent event) {
                Popup __this = this;
                super.onPreviewNativeEvent(event);
                if (event.getTypeInt() == Event.ONKEYDOWN) {
                    int keyCode = event.getNativeEvent().getKeyCode();

                    // 如果窗口正在显示，我们拦截 Escape 和 /
                    if (this.isShowing()) {

                     if (keyCode == KeyCodes.KEY_S && event.getNativeEvent().getCtrlKey()) {
                            this.getContent().saveAndExit(new Callback<Void, Void>() {
                                @Override
                                public void onFailure(Void reason) {

                                }

                                @Override
                                public void onSuccess(Void result) {
                                    __this.hide();
                                }
                            });
                            // Ctrl+S save and escape
                            event.getNativeEvent().stopPropagation();
                            event.getNativeEvent().preventDefault();
                            // 2. 停止事件传播（防止传给外部的 GanttMouseActionDefault）
                            event.getNativeEvent().stopPropagation();
                            // 3. 取消预览事件
                            event.cancel();

                        }
                    }
                }
            }
        };
        widgets.setGlassEnabled(true);
        return widgets;
    }

    private void saveAndExit(Callback<Void, Void> afterSaveHandler) {
        if (btnSave.isEnabled()) {
            afterSave = afterSaveHandler;
            btnSaveClick(null);
        }
    }


    @Override
    protected void onLoad() {
        super.onLoad();
        initEditor();
    }

    /**
     * 初始化编辑器
     */
    private void initEditor() {
        if (!initialize) {
        }
        updateUI();
    }

    private void updateUI() {
        if (task == null) {
            return;
        }
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                txtName.setFocus(true);
                txtName.setSelectionRange(0, task.getName().length());
            }
        });
    }

    private void updateBackground(DevTaskKind kind) {
        if (kind == null) return;
        // 使用 15% 的透明度 (26)，既能看出类型颜色，又不至于刺眼
        back.getStyle().setProperty("background",
                "linear-gradient(180deg, " + kind.getColor() + "26 0%, rgba(255,255,255,0) 100%)");
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(980, 600);
    }

    @UiHandler("member")
    public void memberCommon(CommonEvent event) {
        if (event.isSelect()) {
            ProjectMember projectMember = event.getValue();
            task.setCharger(projectMember.getUserId());
        }
    }

    @UiHandler("btnSave")
    public void btnSaveClick(ClickEvent event) {
        DevProjectTaskEntity temp;
        if (StringUtil.isBlank(task.getId())) {
            temp = task;
        } else {
            temp = new DevProjectTaskEntity();
            temp.setId(task.getId());
            temp.setProjectId(task.getProjectId());
        }
        temp.setName(txtName.getText());
        temp.setKind((Integer) ddlKind.getValue());
        temp.setStatus((Integer) ddlStatus.getValue());
        temp.setSummary(txtSummary.getText());
        temp.setPriority((Integer) ddlPriority.getValue());
        doSave(temp);
    }

    @UiHandler("btnClose")
    public void btnCloseClick(ClickEvent event) {
        fireEvent(CommonEvent.closeEvent(null));
    }

    @UiHandler("txtName")
    public void txtNameKeyDown(KeyDownEvent event) {

        if (btnSave.isEnabled() && event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            btnSave.click();
        }
    }

    private void doSave(DevProjectTaskEntity task) {
        UpdateProjectTaskRequest request = new UpdateProjectTaskRequest();
        request.setProjectTask(task);
        AppProxy.get().updateProjectTask(request, new AsyncCallback<RpcResult<UpdateProjectTaskResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateProjectTaskResponse> result) {
                if (result.isSuccess()) {
                    if (StringUtil.isBlank(task.getId())) {
                        fireEvent(CommonEvent.createEvent(result.getData().getProjectTask()));
                    } else {
                        fireEvent(CommonEvent.updateEvent(result.getData().getProjectTask()));
                    }
                    if (afterSave != null) {
                        fireEvent(CommonEvent.abortEvent(null));
                    } else {
                        setData(result.getData().getProjectTask());
                    }
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    @Override
    public DevProjectTaskEntity getData() {
        return task;
    }

    @Override
    public void setData(DevProjectTaskEntity obj) {
        afterSave = null;
        task = obj;
        toUI();
    }

    private void toUI() {
        if (task == null) return;

        // 基本信息
        txtName.setText(task.getName());
        txtSummary.setText(task.getSummary());

        // 侧边栏属性
        ddlKind.setValue(task.getKind());
        ddlStatus.setValue(task.getStatus()); // 对应 FLD_STATUS
        ddlPriority.setValue(task.getPriority());

        // 负责人
        member.setData(task.getProjectId(), task.getCharger(), task.getChargeUserName(), task.getChargeAvatar());

        // 视觉反馈
        DevTaskKind kind = DevTaskKind.fromCode(task.getKind());
        updateBackground(kind);

    }

    private void updateButtons(DevTaskKind kind) {

        editor.setVisible(true);

        if (task.getId() == null) {
            btnSave.setEnabled(true);
        } else {
            btnSave.setEnabled(true);
        }
    }

    @Override
    public void onResize() {
        root.onResize();
    }


    interface DevTaskEditorUiBinder extends UiBinder<DockLayoutPanel, DevTaskEditor> {
    }
}