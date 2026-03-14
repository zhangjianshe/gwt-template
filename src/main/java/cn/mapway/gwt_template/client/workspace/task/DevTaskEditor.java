package cn.mapway.gwt_template.client.workspace.task;

import cn.mapway.ace.client.AceCommandDescription;
import cn.mapway.ace.client.AceEditor;
import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.workspace.widget.TaskKindDropdown;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskKind;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskPriority;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectMember;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

/**
 * 任务编辑器
 */
public class DevTaskEditor extends CommonEventComposite implements RequiresResize, IData<DevProjectTaskEntity> {
    private static final DevTaskEditorUiBinder ourUiBinder = GWT.create(DevTaskEditorUiBinder.class);
    private static Dialog<DevTaskEditor> dialog;
    @UiField
    TextBox txtName;
    @UiField
    ProjectMemberWidget member;
    @UiField
    TaskKindDropdown ddlKind;
    @UiField
    DivElement back;
    @UiField
    HTMLPanel container;
    @UiField
    HTMLPanel editor;
    @UiField
    HTMLPanel tip;
    @UiField
    AiButton btnCreate;
    @UiField
    AiButton btnCreateSub;
    @UiField
    AiButton btnSave;
    @UiField
    AiButton btnClose;
    @UiField
    AceEditor txtSummary;
    @UiField
    DockLayoutPanel root;
    @UiField
    TabLayoutPanel editArea;
    boolean initialize = false;
    private DevProjectTaskEntity task;

    public DevTaskEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
        btnCreate.setEnabled(false);
        btnCreateSub.setEnabled(false);

        ddlKind.addValueChangeHandler(event -> {
            DevTaskKind kind = DevTaskKind.fromCode((Integer) event.getValue());
            updatebackground(kind);
            updateButtons(kind);
        });
    }

    public static Dialog<DevTaskEditor> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<DevTaskEditor> createOne() {
        return new Dialog<>(new DevTaskEditor(), "任务编辑");
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
            txtSummary.startEditor();
            txtSummary.setShowPrintMargin(false);
            txtSummary.setFontSize(12);
            txtSummary.setUseWorker(false);
            txtSummary.setShowGutter(true);
            txtSummary.setUseWrapMode(true);
            txtSummary.redisplay();
            txtSummary.setFontSize("1.2rem");
            initialize = true;

            AceCommandDescription ctrlSaveCommand = new AceCommandDescription("save", aceEditor -> {
                if (btnSave.isEnabled()) {
                    btnSaveClick(null);
                }
                return null;
            });
            ctrlSaveCommand.withBindKey("Ctrl-S", "Cmd-S");
            txtSummary.addCommand(ctrlSaveCommand);

            AceCommandDescription esc = new AceCommandDescription("Esc", new AceCommandDescription.ExecAction() {
                @Override
                public Object exec(AceEditor editor) {
                    DevTaskEditor.this.fireEvent(CommonEvent.abortEvent(null));
                    return null;
                }
            });
            esc.withBindKey("Escape");
            txtSummary.addCommand(esc);
        }

        txtSummary.redisplay();
        updateUI();
    }

    private void updateUI() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                if (StringUtil.isBlank(task.getId())) {
                    //新的任务
                    txtName.setFocus(true);
                    txtName.setSelectionRange(0, task.getName().length());
                } else {
                    txtSummary.focus();
                }
            }
        });
    }

    private void updatebackground(DevTaskKind kind) {
        back.getStyle().setProperty("background", "linear-gradient(180deg, " + kind.getColor() + ", transparent)");
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(800, 450);
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
        }
        temp.setName(txtName.getText());
        temp.setProjectId(task.getProjectId());
        temp.setCharger(task.getCharger());
        temp.setKind((Integer) ddlKind.getValue());
        temp.setSummary(txtSummary.getText());
        doSave(temp);
    }

    @UiHandler("btnClose")
    public void btnCloseClick(ClickEvent event) {
        fireEvent(CommonEvent.closeEvent(null));
    }

    @UiHandler("btnCreate")
    public void btnCreateClick(ClickEvent event) {
        //创建同级下的新任务
        DevProjectTaskEntity newTask = new DevProjectTaskEntity();
        newTask.setName("新任务");
        newTask.setCharger(task.getCharger());
        newTask.setKind((Integer) ddlKind.getValue());
        newTask.setParentId(task.getParentId());
        newTask.setPriority(DevTaskPriority.MEDIUM.getCode());
        newTask.setParentId(task.getParentId());
        newTask.setProjectId(task.getProjectId());
        setData(newTask);
    }

    @UiHandler("btnCreateSub")
    public void btnCreateSubClick(ClickEvent event) {
        DevProjectTaskEntity newTask = new DevProjectTaskEntity();
        newTask.setName("新任务");
        newTask.setCharger(task.getCharger());
        newTask.setKind((Integer) ddlKind.getValue());
        newTask.setParentId(task.getParentId());
        newTask.setPriority(DevTaskPriority.MEDIUM.getCode());
        newTask.setParentId(task.getId());
        newTask.setProjectId(task.getProjectId());
        setData(newTask);
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
                    setData(result.getData().getProjectTask());
                    if (StringUtil.isBlank(task.getId())) {
                        fireEvent(CommonEvent.createEvent(result.getData().getProjectTask()));
                    } else {
                        fireEvent(CommonEvent.updateEvent(result.getData().getProjectTask()));
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
        task = obj;
        toUI();
    }

    private void toUI() {
        if (task == null) {
            btnSave.setEnabled(false);
            btnCreate.setEnabled(false);
            btnCreateSub.setEnabled(false);
            tip.setVisible(true);
            editor.setVisible(false);
            editArea.setVisible(false);
            return;
        }
        initEditor();
        DevTaskKind kind
                = DevTaskKind.fromCode(task.getKind());
        updateButtons(kind);
        txtName.setText(task.getName());
        member.setData(task.getProjectId(), task.getCharger(), task.getChargeUserName(), task.getChargeAvatar());
        ddlKind.setValue(task.getKind());
        txtSummary.setValue(task.getSummary());
        updatebackground(kind);
        updateUI();

    }

    private void updateButtons(DevTaskKind kind) {

        tip.setVisible(false);
        editor.setVisible(true);
        editArea.setVisible(true);

        if (task.getId() == null) {
            btnSave.setEnabled(true);
            btnCreate.setEnabled(false);
            btnCreateSub.setEnabled(false);
        } else {
            btnSave.setEnabled(true);
            btnCreateSub.setEnabled(true);
            btnCreate.setEnabled(true);
        }

        if (kind == DevTaskKind.DTK_SUMMARY || kind == DevTaskKind.DTK_MILESTONE) {
            btnCreateSub.setEnabled(false);
        }
    }

    @Override
    public void onResize() {
        root.onResize();
    }

    interface DevTaskEditorUiBinder extends UiBinder<DockLayoutPanel, DevTaskEditor> {
    }
}