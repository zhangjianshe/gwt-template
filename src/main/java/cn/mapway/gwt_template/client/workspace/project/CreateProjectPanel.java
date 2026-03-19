package cn.mapway.gwt_template.client.workspace.project;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.workspace.widget.SecurityLevelDropdown;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryTemplateProjectRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryTemplateProjectResponse;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevProjectRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevProjectResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.util.Colors;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.DateBox;
import lombok.Setter;

import java.util.Date;

/**
 * 创建项目对话框
 */
public class CreateProjectPanel extends CommonEventComposite {
    private static final CreateProjectPanelUiBinder ourUiBinder = GWT.create(CreateProjectPanelUiBinder.class);
    private static Popup<CreateProjectPanel> dialog;
    @UiField
    DeckLayoutPanel deck;
    @UiField
    HTMLPanel pnlTemplates;
    @UiField
    TextBox txtName;
    @UiField
    Button btnBack, btnNext, btnCreate;
    @UiField
    Label lbTitle;
    @UiField
    Label lbMessage;
    @UiField
    Label tip;
    @UiField
    TextArea txtSummary;
    @UiField
    DateBox txtStartDate;
    @UiField
    SecurityLevelDropdown ddlSecurity;
    ProjectTemplateItem currentSelected = null;
    @Setter
    String workspaceId;
    @Setter
    String folderId;
    ClickHandler itemClicked = event -> {
        ProjectTemplateItem source = (ProjectTemplateItem) event.getSource();

        selectItem(source);
        goToStep(1);
    };

    public CreateProjectPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
        deck.showWidget(0);
        btnNext.setEnabled(false);
        txtStartDate.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy年MM月dd")));
        Date date = new Date();
        date.setTime(date.getTime() + 3 * 24 * 60 * 60 * 1000);
        txtStartDate.setValue(date);
    }

    public static Popup<CreateProjectPanel> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Popup<CreateProjectPanel> createOne() {
        CreateProjectPanel panel = new CreateProjectPanel();
        return new Popup<>(panel);
    }

    public void setCreateParameter(String workspaceId, String folderId) {
        this.workspaceId = StringUtil.trim(workspaceId);
        this.folderId = StringUtil.trim(folderId);
        goToStep(0);
    }

    private void selectItem(ProjectTemplateItem source) {
        if (currentSelected != null) {
            currentSelected.setSelect(false);
        }
        currentSelected = source;
        if (currentSelected != null) {
            currentSelected.setSelect(true);

        }
        if (currentSelected != null) {
            if (currentSelected.getData() != null) {
                lbMessage.setText("当前选择" + currentSelected.getData().getName());
            } else {
                lbMessage.setText("当前选择 空白项目");
            }
        }
    }

    public void loadTemplates() {
        QueryTemplateProjectRequest request = new QueryTemplateProjectRequest();
        AppProxy.get().queryTemplateProject(request, new AsyncCallback<RpcResult<QueryTemplateProjectResponse>>() {
            @Override
            public void onFailure(Throwable caught) {

            }

            @Override
            public void onSuccess(RpcResult<QueryTemplateProjectResponse> result) {
                if (result.isSuccess()) {
                    pnlTemplates.clear();
                    pnlTemplates.add(createTemplateCard(null));
                    for (DevProjectEntity projectEntity : result.getData().getProjects()) {
                        pnlTemplates.add(createTemplateCard(projectEntity));
                    }
                }
            }
        });
    }

    private ProjectTemplateItem createTemplateCard(DevProjectEntity template) {
        ProjectTemplateItem item = new ProjectTemplateItem();
        item.setData(template);
        item.setPixelSize(180, 140);
        item.addDomHandler(itemClicked, ClickEvent.getType());
        return item;
    }


    @UiHandler("btnNext")
    void onNext(ClickEvent e) {
        goToStep(1);
    }

    @UiHandler("btnBack")
    void onBack(ClickEvent e) {
        goToStep(0);
    }

    private void goToStep(int step) {
        deck.showWidget(step);
        boolean isStep2 = (step == 1);
        btnBack.setVisible(isStep2);
        btnNext.setVisible(!isStep2);
        btnCreate.setVisible(isStep2);
        lbTitle.setText(isStep2 ? "第二步：填写项目信息" : "第一步：选择项目模板");
        if (isStep2) {
            txtName.setFocus(true);
            if (currentSelected != null) {
                if (currentSelected.getData() == null) {
                    showNewCreateTip();
                } else {
                    txtName.setText(currentSelected.getData().getName() + "_复本");
                    tip.setText("从模板 " + currentSelected.getData().getName() + " 创建");
                }
            } else {
                showNewCreateTip();
            }
        } else {
            btnNext.setEnabled(currentSelected != null);
        }

    }

    private void showNewCreateTip() {
        txtName.setValue("空白项目");
        tip.setText("创建新的空白项目");
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(900, 600);
    }

    @UiHandler("btnCreate")
    void onCreate(ClickEvent e) {
        String name = txtName.getText();
        if (StringUtil.isBlank(name)) {
            lbMessage.setText("请输入名称");
        }
        UpdateDevProjectRequest request = new UpdateDevProjectRequest();
        DevProjectEntity project = new DevProjectEntity();
        project.setName(name);
        project.setSummary(txtSummary.getValue());
        project.setColor(Colors.randomColor());
        project.setUnicode(Fonts.PROJECT);
        project.setFolderId(folderId);
        project.setWorkspaceId(workspaceId);
        project.setTag("");
        project.setSecurityLevel((Integer) ddlSecurity.getValue());
        request.setProject(project);
        btnCreate.setEnabled(false);
        if (currentSelected != null && currentSelected.getData() != null) {
            request.setTemplateId(currentSelected.getData().getId());
        } else {
            request.setTemplateId(null);
        }
        request.setTargetStartTime(txtStartDate.getValue().getTime());

        AppProxy.get().updateDevProject(request, new AsyncCallback<RpcResult<UpdateDevProjectResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                lbMessage.setText(caught.getMessage());
                btnCreate.setEnabled(true);
            }

            @Override
            public void onSuccess(RpcResult<UpdateDevProjectResponse> result) {
                if (result.isSuccess()) {
                    fireEvent(CommonEvent.reloadEvent(result.getData().getProject()));
                    btnCreate.setEnabled(true);
                } else {
                    lbMessage.setText(result.getMessage());
                    btnCreate.setEnabled(true);
                }
            }
        });
    }

    interface CreateProjectPanelUiBinder extends UiBinder<DockLayoutPanel, CreateProjectPanel> {
    }
}