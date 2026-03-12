package cn.mapway.gwt_template.client.widget;

import cn.mapway.ace.client.AceCommandDescription;
import cn.mapway.ace.client.AceEditor;
import cn.mapway.ace.client.AceEditorMode;
import cn.mapway.ace.client.EditorOption;
import cn.mapway.ui.client.mvc.IToolsProvider;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.mvc.attribute.editor.inspector.CodeLanguage;
import cn.mapway.ui.client.mvc.attribute.editor.inspector.LanguageDropdown;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

/**
 * 数据编辑对话框
 */
public class DataEditorDialog extends CommonEventComposite implements IData<String>, RequiresResize, IToolsProvider {
    private static final DataEditorDialogUiBinder ourUiBinder = GWT.create(DataEditorDialogUiBinder.class);
    private static Dialog<DataEditorDialog> dialog;
    @UiField
    AceEditor dataEditor;
    @UiField
    SaveBar saveBar;
    @UiField
    DockLayoutPanel root;
    @UiField
    HorizontalPanel tools;

    @UiField
    LanguageDropdown language;
    boolean initialized = false;

    public DataEditorDialog() {
        initWidget(ourUiBinder.createAndBindUi(this));
        language.addValueChangeHandler(event -> setLanguage((CodeLanguage) event.getValue()));
    }

    public static Dialog<DataEditorDialog> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            dialog.getContent().enableSave(true);
            dialog.getContent().saveBar.setSaveText("保存");
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<DataEditorDialog> createOne() {
        DataEditorDialog editor = new DataEditorDialog();
        return new Dialog<>(editor, "数据编辑");
    }

    public void enableSave(boolean b) {
        saveBar.enableSave(b);
    }

    public void setSaveText(String text) {
        saveBar.setSaveText(text);
    }

    /**
     * @return
     */
    @Override
    public String getData() {
        return null;
    }

    /**
     * @param s
     */
    @Override
    public void setData(String s) {
        dataEditor.setValue(s);
    }


    public void setTabIndent(boolean enable){
        dataEditor.setUseSoftTabs(!enable);
    }

    public void setLanguage(CodeLanguage codeLanguage) {
        if (codeLanguage == null) {
            dataEditor.setMode(AceEditorMode.TEXT);
            return;
        }
        language.setValue(codeLanguage, false);
        switch (codeLanguage) {
            case JS:
                dataEditor.setMode(AceEditorMode.JAVASCRIPT);
                break;
            case PYTHON:
                dataEditor.setMode(AceEditorMode.PYTHON);
                break;
            case JSON:
                dataEditor.setMode(AceEditorMode.JSON);
                break;
            case XML:
                dataEditor.setMode(AceEditorMode.XML);
                break;
            case CSS:
                dataEditor.setMode(AceEditorMode.CSS);
                break;
            case JAVA:
                dataEditor.setMode(AceEditorMode.JAVA);
                break;
            case HTML:
                dataEditor.setMode(AceEditorMode.HTML);
                break;
            case SHELL:
                dataEditor.setMode(AceEditorMode.SH);
                break;
            case SQL:
                dataEditor.setMode(AceEditorMode.SQL);
                break;
            case YML:
                dataEditor.setMode(AceEditorMode.YAML);
                break;
            default:
                dataEditor.setMode(AceEditorMode.TEXT);
        }

    }

    @Override
    protected void onLoad() {
        super.onLoad();
        if (!initialized) {
            initialized = true;
            dataEditor.startEditor();
            dataEditor.setShowPrintMargin(false);
            dataEditor.setFontSize("1.1rem");
            dataEditor.setUseWorker(false);
            dataEditor.setShowGutter(true);
            dataEditor.setUseWrapMode(true);
            EditorOption option = new EditorOption();
            option.enableBasicAutocompletion(true).enableLiveAutocompletion(true).enableSnippets(true);
            dataEditor.setOptions(option);
            AceCommandDescription ctrlSaveCommand = new AceCommandDescription("save", new AceCommandDescription.ExecAction() {
                @Override
                public Object exec(AceEditor aceEditor) {
                    String value = dataEditor.getValue();
                    fireEvent(CommonEvent.saveEvent(value));
                    return true;
                }
            });
            ctrlSaveCommand.withBindKey("Ctrl-S", "Cmd-S");
            dataEditor.addCommand(ctrlSaveCommand);
            dataEditor.redisplay();
        }
    }

    /**
     *
     */
    @Override
    public void onResize() {
        root.onResize();
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(900, 700);
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            String value = dataEditor.getValue();
            fireEvent(CommonEvent.okEvent(value));
        } else {
            fireEvent(event);
        }
    }


    public void setToolbarVisible(boolean visible) {
        if (visible) {
            root.setWidgetSize(tools, 40);
        } else {
            root.setWidgetSize(tools, 0);
        }
    }

    /**
     * @return
     */
    @Override
    public Widget getTools() {
        return tools;
    }


    public void setMessage(String message) {
        saveBar.msg(message);
    }

    interface DataEditorDialogUiBinder extends UiBinder<DockLayoutPanel, DataEditorDialog> {
    }
}