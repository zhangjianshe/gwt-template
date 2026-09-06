package cn.mapway.gwt_template.client.software;

import cn.mapway.gwt_template.client.widget.TextBox;
import cn.mapway.gwt_template.client.widget.file.CommonFileUploadResult;
import cn.mapway.gwt_template.client.widget.file.XmlHttpUploader;
import cn.mapway.gwt_template.shared.db.SysSoftwareEntity;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import elemental2.core.Global;
import elemental2.dom.File;
import elemental2.dom.FileList;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.ProgressEvent;
import elemental2.dom.XMLHttpRequest;
import jsinterop.base.Js;

public class SoftwareFileUploader extends CommonEventComposite implements IData<SysSoftwareEntity> {
    private static final String[] OS_OPTIONS = {"linux", "windows", "darwin", "all"};
    private static final String[] ARCH_OPTIONS = {"amd64", "arm64", "aarch64", "x86_64", "all"};
    private static final SoftwareFileUploaderUiBinder ourUiBinder = GWT.create(SoftwareFileUploaderUiBinder.class);
    private static Dialog<SoftwareFileUploader> dialog;
    @UiField
    SaveBar saveBar;
    @UiField
    Label lbSoftware;
    @UiField
    TextBox txtVersion;
    @UiField
    ListBox lstArch;
    @UiField
    ListBox lstOs;
    @UiField
    FileUpload fileUpload;
    @UiField
    Button btnPickFile;
    @UiField
    Label lbFileName;
    private SysSoftwareEntity software;
    private File selectedFile;
    private XmlHttpUploader xmlHttpUploader;
    private final CommonEventHandler uploadHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent event) {
            if (event.isProgress()) {
                ProgressEvent ev = Js.cast(event.getValue());
                if (ev.total > 0) {
                    int percent = (int) (ev.loaded * 100.0 / ev.total);
                    saveBar.msg("上传中 " + percent + "%");
                }
            } else if (event.isReadyChange()) {
                XMLHttpRequest request = xmlHttpUploader.getRequest();
                if (request.readyState == XMLHttpRequest.DONE) {
                    handleUploadDone(request);
                }
            } else if (event.isError() || event.isAbort()) {
                saveBar.enableSave(true);
                saveBar.msg(event.isAbort() ? "已取消上传" : "上传失败");
            }
        }
    };

    public SoftwareFileUploader() {
        initWidget(ourUiBinder.createAndBindUi(this));
        fillOptions(lstOs, OS_OPTIONS, "linux");
        fillOptions(lstArch, ARCH_OPTIONS, "amd64");
        txtVersion.getElement().setAttribute("placeholder", "latest");
        txtVersion.setValue("latest");
        saveBar.setSaveText("上传");
        xmlHttpUploader = new XmlHttpUploader();
        xmlHttpUploader.addCommonHandler(uploadHandler);
        xmlHttpUploader.setAction(GWT.getHostPageBaseURL() + "api/v1/software/upload");
        xmlHttpUploader.setFileFieldName("file");
    }

    public static Dialog<SoftwareFileUploader> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        }
        return createOne();
    }

    private static Dialog<SoftwareFileUploader> createOne() {
        return new Dialog<>(new SoftwareFileUploader(), "上传软件文件");
    }

    private void fillOptions(ListBox listBox, String[] options, String selected) {
        listBox.clear();
        int selectedIndex = 0;
        for (int i = 0; i < options.length; i++) {
            listBox.addItem(options[i]);
            if (options[i].equals(selected)) {
                selectedIndex = i;
            }
        }
        listBox.setSelectedIndex(selectedIndex);
    }

    @UiHandler("fileUpload")
    public void fileUploadChange(ChangeEvent event) {
        HTMLInputElement element = Js.uncheckedCast(fileUpload.getElement());
        FileList files = element.files;
        if (files != null && files.length > 0) {
            selectedFile = files.item(0);
            lbFileName.setText(selectedFile.name + " (" + StringUtil.formatFileSize((long) selectedFile.size) + ")");
        } else {
            selectedFile = null;
            lbFileName.setText("");
        }
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            startUpload();
        } else {
            if (xmlHttpUploader.isTransfering()) {
                xmlHttpUploader.abort();
            }
            fireEvent(event);
        }
    }

    private void startUpload() {
        if (software == null || StringUtil.isBlank(software.getToken())) {
            saveBar.msg("没有选择软件");
            return;
        }
        String version = trim(txtVersion.getValue());
        String os = selectedValue(lstOs);
        String arch = selectedValue(lstArch);
        if (StringUtil.isBlank(version)) {
            saveBar.msg("请输入版本");
            return;
        }
        if (StringUtil.isBlank(os)) {
            saveBar.msg("请选择操作系统");
            return;
        }
        if (StringUtil.isBlank(arch)) {
            saveBar.msg("请选择架构");
            return;
        }
        if (selectedFile == null) {
            saveBar.msg("请选择文件");
            return;
        }
        xmlHttpUploader.clearData();
        xmlHttpUploader.setData("token", software.getToken());
        xmlHttpUploader.setData("version", version);
        xmlHttpUploader.setData("os", os);
        xmlHttpUploader.setData("arch", arch);
        xmlHttpUploader.setData("name", selectedFile.name);
        xmlHttpUploader.setData("summary", "");
        saveBar.enableSave(false);
        saveBar.msg("开始上传");
        xmlHttpUploader.start(selectedFile);
    }

    private void handleUploadDone(XMLHttpRequest request) {
        saveBar.enableSave(true);
        if (request.status == 0) {
            return;
        }
        if (request.status != 200) {
            saveBar.msg("上传失败 " + request.status);
            return;
        }
        String responseText = request.responseText;
        if (StringUtil.isBlank(responseText)) {
            saveBar.msg("上传失败：空响应");
            return;
        }
        CommonFileUploadResult result;
        try {
            result = Js.uncheckedCast(Global.JSON.parse(responseText));
        } catch (Exception e) {
            saveBar.msg("上传失败：无法解析响应");
            return;
        }
        if (result == null || result.code != 200) {
            saveBar.msg(result == null || result.message == null ? "上传失败" : result.message);
            return;
        }
        saveBar.msg("上传成功");
        fireEvent(CommonEvent.okEvent(software));
    }

    private String selectedValue(ListBox listBox) {
        int index = listBox.getSelectedIndex();
        if (index < 0) {
            return "";
        }
        return listBox.getValue(index);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(600, 420);
    }

    @Override
    public SysSoftwareEntity getData() {
        return software;
    }

    @Override
    public void setData(SysSoftwareEntity obj) {
        software = obj;
        lbSoftware.setText(software == null ? "" : software.getName());
        selectedFile = null;
        lbFileName.setText("未选择文件");
        HTMLInputElement element = Js.uncheckedCast(fileUpload.getElement());
        element.value = "";
        saveBar.enableSave(true);
        saveBar.msg("");
        if (StringUtil.isBlank(txtVersion.getValue())) {
            txtVersion.setValue("latest");
        }
    }

    interface SoftwareFileUploaderUiBinder extends UiBinder<DockLayoutPanel, SoftwareFileUploader> {
    }
}
