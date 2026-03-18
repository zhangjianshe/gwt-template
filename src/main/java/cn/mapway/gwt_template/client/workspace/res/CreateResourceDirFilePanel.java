package cn.mapway.gwt_template.client.workspace.res;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.rpc.project.res.CreateProjectDirFileRequest;
import cn.mapway.gwt_template.shared.rpc.project.res.CreateProjectDirFileResponse;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.AiTextBox;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;

public class CreateResourceDirFilePanel extends CommonEventComposite {
    private static final CreateResourceDirFilePanelUiBinder ourUiBinder = GWT.create(CreateResourceDirFilePanelUiBinder.class);
    private static Dialog<CreateResourceDirFilePanel> dialog;
    @UiField
    SaveBar saveBar;
    @UiField
    AiTextBox txtFileName;
    @UiField
    AiTextBox txtDirName;
    @UiField
    TabLayoutPanel tab;
    String resourceId;
    String relativePath;

    public CreateResourceDirFilePanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Dialog<CreateResourceDirFilePanel> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<CreateResourceDirFilePanel> createOne() {
        CreateResourceDirFilePanel dialog = new CreateResourceDirFilePanel();
        return new Dialog<>(dialog, "创建文件或文件夹");
    }

    public void setContextData(String resourceId, String relativePath) {
        this.resourceId = resourceId;
        this.relativePath = relativePath;
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(560, 380);
    }

    public void init() {
        txtDirName.setValue("");
        txtFileName.setValue("");
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            doSave();
        } else {
            fireEvent(event);
        }
    }

    private void doSave() {
        if (StringUtil.isBlank(resourceId)) {
            saveBar.msg("没有设置资源分类ID");
        }
        if (tab.getSelectedIndex() == 0) {
            // create file
            String fileName = txtFileName.getValue();
            if (!checkFileName(fileName)) {
            } else {
                doCreate(fileName, false);
            }
        } else {
            String fileName = txtDirName.getValue();
            if (!checkFileName(fileName)) {
            } else {
                doCreate(fileName, true);
            }
        }
    }

    private void doCreate(String fileName, boolean createDir) {
        CreateProjectDirFileRequest request = new CreateProjectDirFileRequest();
        request.setResourceId(resourceId);
        request.setParentPath(relativePath);
        request.setName(fileName);
        request.setIsDir(createDir);
        AppProxy.get().createProjectDirFile(request, new AsyncCallback<RpcResult<CreateProjectDirFileResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                saveBar.msg(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<CreateProjectDirFileResponse> result) {
                if (result.isSuccess()) {
                    fireEvent(CommonEvent.updateEvent(null));
                } else {
                    saveBar.msg(result.getMessage());
                }
            }
        });
    }

    private boolean checkFileName(String fileName) {
        if (StringUtil.isBlank(fileName)) {
            saveBar.msg("目录名称不满足要求");
            return false;
        }
        fileName = fileName.trim();
        fileName = fileName.replaceAll(" ", "");
        RegExp regExp = RegExp.compile("^(\\w+\\.?)*\\w+$");
        boolean test = regExp.test(fileName);
        if (!test) {
            saveBar.msg("目录名称不满足要求");
            return false;
        }
        return true;
    }

    interface CreateResourceDirFilePanelUiBinder extends UiBinder<DockLayoutPanel, CreateResourceDirFilePanel> {
    }
}