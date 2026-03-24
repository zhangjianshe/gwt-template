package cn.mapway.gwt_template.client.workspace.project;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.workspace.WorkspaceFolderDropdown;
import cn.mapway.gwt_template.client.workspace.widget.SecurityLevelDropdown;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.db.DevWorkspaceFolderEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevProjectRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevProjectResponse;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.mvc.attribute.DataCastor;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.AiTextBox;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.ImageUploader;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.UploadReturn;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.TextArea;

import java.util.List;

public class DevProjectEditor extends CommonEventComposite implements IData<DevProjectEntity> {
    private static final DevProjectEditorUiBinder ourUiBinder = GWT.create(DevProjectEditorUiBinder.class);
    private static Dialog<DevProjectEditor> dialog;
    DevProjectEntity project;
    @UiField
    SaveBar saveBar;
    @UiField
    AiTextBox txtColor;
    @UiField
    AiTextBox txtName;
    @UiField
    TextArea txtSummary;
    @UiField
    ImageUploader uploader;
    @UiField
    CheckBox checkTemplate;
    @UiField
    WorkspaceFolderDropdown ddlFolder;
    @UiField
    SecurityLevelDropdown ddlSecurity;

    public DevProjectEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
        txtColor.asColor();
        uploader.setAction(GWT.getHostPageBaseURL() + "fileUpload", "project");
    }

    public static Dialog<DevProjectEditor> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }

    }

    public static Dialog<DevProjectEditor> createOne() {
        return new Dialog<>(new DevProjectEditor(), "编辑项目");
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(600, 650);
    }

    @Override
    public DevProjectEntity getData() {
        return project;
    }

    @Override
    public void setData(DevProjectEntity obj) {
        project = obj;
        toUI();
    }

    public void initFolder(List<DevWorkspaceFolderEntity> folders) {
        ddlFolder.init(folders);
    }

    @UiHandler("uploader")
    public void uploaderCommon(CommonEvent event) {
        if (event.isOk()) {
            UploadReturn uploadReturn = event.getValue();
            project.setIcon(uploadReturn.relPath);
        }
    }

    private void toUI() {
        txtColor.setValue(project.getColor());
        txtName.setValue(project.getName());
        txtSummary.setValue(project.getSummary());
        uploader.setUrl(project.getIcon());
        checkTemplate.setValue(project.getIsTemplate());
        ddlFolder.setValue(project.getFolderId());
        ddlSecurity.setValue(project.getSecurityLevel());
    }

    private void fromUI() {
        project.setColor(txtColor.getValue());
        project.setName(txtName.getValue());
        project.setSummary(txtSummary.getValue());
        project.setIsTemplate(checkTemplate.getValue());
        project.setFolderId(DataCastor.castToString(ddlFolder.getValue()));
        project.setSecurityLevel((Integer) ddlSecurity.getValue());
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            fromUI();
            UpdateDevProjectRequest request = new UpdateDevProjectRequest();
            request.setProject(project);
            AppProxy.get().updateDevProject(request, new AsyncCallback<RpcResult<UpdateDevProjectResponse>>() {
                @Override
                public void onFailure(Throwable caught) {
                    saveBar.msg(caught.getMessage());
                }

                @Override
                public void onSuccess(RpcResult<UpdateDevProjectResponse> result) {
                    if (result.isSuccess()) {
                        fireEvent(CommonEvent.updateEvent(result.getData().getProject()));
                    } else {
                        saveBar.msg(result.getMessage());
                    }
                }
            });
        } else {
            fireEvent(event);
        }
    }

    interface DevProjectEditorUiBinder extends UiBinder<DockLayoutPanel, DevProjectEditor> {
    }
}