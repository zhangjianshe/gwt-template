package cn.mapway.gwt_template.client.workspace.res.viewer;

import cn.mapway.ace.client.AceCommandDescription;
import cn.mapway.ace.client.AceEditor;
import cn.mapway.ace.client.AceEditorMode;
import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.rpc.file.EditableFileSuffix;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectFileRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectFileResponse;
import cn.mapway.gwt_template.shared.rpc.project.res.ViewProjectFileResponse;
import cn.mapway.ui.client.mvc.IToolsProvider;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;


public class TextEditViewer extends CommonEventComposite implements RequiresResize, IToolsProvider {
    private static final TextEditViewerUiBinder ourUiBinder = GWT.create(TextEditViewerUiBinder.class);
    @UiField
    AceEditor editor;
    @UiField
    Button btnSave;
    @UiField
    HorizontalPanel tools;
    @UiField
    DockLayoutPanel root;
    boolean initialize = false;
    private ViewProjectFileResponse data;

    public TextEditViewer() {
        initWidget(ourUiBinder.createAndBindUi(this));

    }

    @UiHandler("btnSave")
    public void btnSaveClick(ClickEvent event) {
        doSave();
    }

    public void enableSave(boolean enable) {
        btnSave.setEnabled(enable);
    }

    private void doSave() {
        UpdateProjectFileRequest request = new UpdateProjectFileRequest();
        request.setBody(editor.getValue());
        request.setResourceId(data.getResourceId());
        request.setFilePathName(data.getFileName());
        AppProxy.get().updateProjectFile(request, new AsyncCallback<RpcResult<UpdateProjectFileResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateProjectFileResponse> result) {
                if (result.isSuccess()) {
                    ClientContext.get().toast(0, 0, "保存成功");
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    @Override
    public Widget getTools() {
        return tools;
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        initEditor();
    }

    private void initEditor() {
        if (!initialize) {
            editor.startEditor();

            editor.setShowPrintMargin(false);
            editor.setFontSize(12);
            editor.setUseWorker(false);
            editor.setShowGutter(true);
            editor.setUseWrapMode(true);
            editor.redisplay();
            editor.setFontSize("1.2rem");
            initialize = true;

            AceCommandDescription ctrlSaveCommand = new AceCommandDescription("save", new AceCommandDescription.ExecAction() {
                @Override
                public Object exec(AceEditor aceEditor) {
                    doSave();
                    return true;
                }
            });
            ctrlSaveCommand.withBindKey("Ctrl-S", "Cmd-S");
            editor.addCommand(ctrlSaveCommand);
        }
        editor.redisplay();
    }

    public void setEditorData(ViewProjectFileResponse obj, EditableFileSuffix mode) {
        data = obj;
        toUI();
        if (mode == EditableFileSuffix.NONE) {
            editor.setMode(AceEditorMode.TEXT);
        } else {
            editor.setMode(mode.getMode());
        }
    }

    private void toUI() {
        initEditor();
        editor.setValue(data.getBody());
    }


    @Override
    public void onResize() {
        root.onResize();
    }

    interface TextEditViewerUiBinder extends UiBinder<DockLayoutPanel, TextEditViewer> {
    }
}