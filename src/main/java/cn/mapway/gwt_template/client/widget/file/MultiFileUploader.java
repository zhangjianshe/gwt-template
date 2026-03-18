package cn.mapway.gwt_template.client.widget.file;


import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.ui.client.event.MessageObject;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.util.Colors;
import cn.mapway.ui.client.util.Logs;
import cn.mapway.ui.client.widget.BigFileUploadReturn;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DataTransfer;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.dom.*;
import elemental2.dom.ProgressEvent;
import jsinterop.base.Js;

import java.util.List;

/**
 * MultiFileUploader
 * 多文件上传
 *
 * @author zhang
 */
public class MultiFileUploader extends CommonEventComposite implements RequiresResize {
    private static final MultiFileUploaderUiBinder ourUiBinder = GWT.create(MultiFileUploaderUiBinder.class);
    private static Dialog<MultiFileUploader> dialog;
    @UiField
    FileUpload htmlUploader;
    @UiField
    Button uploadButton;
    @UiField
    HTMLPanel emptyPanel;
    @UiField
    HTMLPanel taskPanel;
    @UiField
    UploadSummary uploadSummary;
    @UiField
    Label lbSubInfo;
    @UiField
    Button btnClose;
    @UiField
    Label lbMessage;
    @UiField
    HTMLPanel dropArea;
    @UiField
    DockLayoutPanel root;
    SummaryData summaryData;
    XmlHttpUploader xmlHttpUploader;
    private final CommonEventHandler uploadItemHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent event) {
            if (event.isDelete()) {
                FileUploadItem item = (FileUploadItem) event.getSource();
                taskPanel.remove(item);
                summaryData.queueSize--;
                summaryData.totalSize -= item.getFile().size;
                if (xmlHttpUploader.getData() == item) {
                    xmlHttpUploader.abort();
                }
            }
        }
    };
    private final ChangeHandler fileChangeHandler = new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent event) {
            taskPanel.clear();
            summaryData = new SummaryData();
            summaryData.setStartTime(System.currentTimeMillis());
            HTMLInputElement element = Js.uncheckedCast(htmlUploader.getElement());
            FileList fileList = element.files;
            if (fileList != null && fileList.length > 0) {
                for (int i = 0; i < fileList.length; i++) {
                    File item = fileList.item(i);
                    FileUploadItem fileUploadItem = new FileUploadItem();
                    fileUploadItem.setFile(item);
                    fileUploadItem.addCommonHandler(uploadItemHandler);

                    taskPanel.add(fileUploadItem);
                    summaryData.totalSize = summaryData.totalSize + item.size;
                    summaryData.queueSize++;
                }
                uploadButton.setEnabled(false);
            } else {
                taskPanel.add(emptyPanel);
                uploadButton.setEnabled(true);
            }
            uploadSummary.setData(summaryData);
            startTransfer();
        }
    };
    private final CommonEventHandler uploadHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent event) {
            if (event.isProgress()) {
                ProgressEvent ev = Js.cast(event.getValue());
                FileUploadItem item = (FileUploadItem) xmlHttpUploader.getData();
                item.setUploadSize((long) ev.loaded);
                if (ev.loaded == ev.total) {
                    summaryData.hasUploadSize += (long) ev.total;
                    summaryData.setUploadedSize(0);
                } else {
                    summaryData.setUploadedSize((long) ev.loaded);
                }
                uploadSummary.setData(summaryData);
            } else if (event.isLoadEnd()) {
                startTransfer();
            } else if (event.isAbort()) {
                //重新计算进度信息
                summaryData.setUploadedSize(0);
                startTransfer();
            } else if (event.isError()) {
                summaryData.setUploadedSize(0);
                startTransfer();
            } else if (event.isReadyChange()) {
                XMLHttpRequest request = xmlHttpUploader.request;
                if (request.readyState == XMLHttpRequest.DONE && request.status == 200) {
                    //成功返回
                    BigFileUploadReturn data = (BigFileUploadReturn) Global.JSON.parse(request.responseText);
                    if (data.code != 200) {
                        CommonEvent ev = CommonEvent.messageEvent(0, MessageObject.CODE_FAIL, data.message);
                        fireEvent(ev);
                        ClientContext.get().toast(0, 0, data.message);
                    }
                }
            }
        }
    };
    boolean renameFlag = false;
    // 文件类型控制
    List<String> subNames;

    public MultiFileUploader() {
        initWidget(ourUiBinder.createAndBindUi(this));

        htmlUploader.addChangeHandler(fileChangeHandler);
        htmlUploader.getElement().setAttribute("multiple", "multiple");
        htmlUploader.addMouseOverHandler(event -> uploadButton.getElement().getStyle().setBackgroundColor("#007bc3"));
        htmlUploader.addMouseOutHandler(event -> uploadButton.getElement().getStyle().setBackgroundColor("#32a9e5"));
        xmlHttpUploader = new XmlHttpUploader();
        xmlHttpUploader.addCommonHandler(uploadHandler);
        initDropEvent();
    }

    public static Dialog<MultiFileUploader> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<MultiFileUploader> createOne() {
        MultiFileUploader uploader = new MultiFileUploader();
        return new Dialog<>(uploader, "上传文件");
    }

    public static native JsArray<File> getDroppedFiles(NativeEvent event) /*-{
        return event.target.files || event.dataTransfer.files;
    }-*/;

    private void initDropEvent() {
        dropArea.addDomHandler(event -> {
            event.stopPropagation();
            event.preventDefault();
            dropArea.getElement().getStyle().setBackgroundColor(Colors.COLOR_GREEN);
            Logs.info("onDropEnter");
        }, DragEnterEvent.getType());
        dropArea.addDomHandler(event -> {
            event.stopPropagation();
            event.preventDefault();
            dropArea.getElement().getStyle().setBackgroundColor("background-color: rgb(255, 255, 255, 0.5)");
            Logs.info("onDragLeave");
        }, DragLeaveEvent.getType());
        dropArea.addDomHandler(event -> {
            event.preventDefault();
            event.stopPropagation();
            dropArea.getElement().getStyle().setBackgroundColor(Colors.COLOR_GREEN);
            NativeEvent nativeEvent = event.getNativeEvent();
            nativeEvent.getDataTransfer().setDropEffect(DataTransfer.DropEffect.COPY);
        }, DragOverEvent.getType());
        dropArea.addDomHandler(event -> {
            event.stopPropagation();
            event.preventDefault();
            dropArea.getElement().getStyle().setBackgroundColor("background-color: rgb(255, 255, 255, 0.5)");
            taskPanel.clear();
            summaryData = new SummaryData();
            summaryData.setStartTime(System.currentTimeMillis());
            JsArray<File> files = getDroppedFiles(event.getNativeEvent());
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    File file = files.getAt(i);
                    // 进行后缀校验

                    if (subNames != null && subNames.size() == 0) {
                        String name = file.name;
                        boolean flag = false;
                        for (String subName : subNames) {
                            if (name.endsWith(subName)) {
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            continue;
                        }
                    }
                    FileUploadItem fileUploadItem = new FileUploadItem();
                    fileUploadItem.setFile(file);
                    fileUploadItem.addCommonHandler(uploadItemHandler);

                    taskPanel.add(fileUploadItem);
                    summaryData.totalSize = summaryData.totalSize + file.size;
                    summaryData.queueSize++;
                }
                uploadButton.setEnabled(false);
            } else {
                taskPanel.add(emptyPanel);
                uploadButton.setEnabled(true);
            }
            uploadSummary.setData(summaryData);
            startTransfer();
        }, DropEvent.getType());
    }

    /**
     * 开始上传
     * 如果没有文件，则不上传
     */
    private void startTransfer() {
        for (int i = 0; i < taskPanel.getWidgetCount(); i++) {
            FileUploadItem item = (FileUploadItem) taskPanel.getWidget(i);
            if (!item.isFinished()) {
                dispatchTransferItem(item);
                return;
            }
        }
        uploadButton.setEnabled(true);
        taskPanel.clear();
        taskPanel.add(emptyPanel);
        lbSubInfo.setText("上传任务已完成 共上传" + summaryData.queueSize + "个文件");
        fireEvent(CommonEvent.refreshEvent(null));
    }

    /**
     * 使用 XMLHTTP2 进行传输
     *
     * @param item
     */
    private void dispatchTransferItem(FileUploadItem item) {
        if (!xmlHttpUploader.isTransfering()) {
            xmlHttpUploader.setData(item);
            xmlHttpUploader.start(item.getFile());
        }
    }

    public void setHeads(String key, String value) {
        xmlHttpUploader.setHeads(key, value);
    }

    public void clearHeads() {
        xmlHttpUploader.clearHeads();
    }

    public void setData(String key, String value) {
        xmlHttpUploader.setData(key, value);
    }

    public void clearData() {
        xmlHttpUploader.clearData();
    }

    public void setAction(String action) {
        xmlHttpUploader.setAction(action);
        //设置活动路径后 缺省设置当前登录用户信息
        xmlHttpUploader.clearHeads();
    }

    public void setFileFieldName(String fileFieldName) {
        xmlHttpUploader.setFileFieldName(fileFieldName);
    }


    /**
     * 用户的图片上传数据目录
     *
     */
    public void setPath(String path) {
        clearData();
        setData("path", path);
        lbSubInfo.setText("选择文件 可以多选 ");
        taskPanel.clear();
        taskPanel.add(emptyPanel);
        uploadButton.setEnabled(true);
    }

    @UiHandler("btnClose")
    public void btnCloseClick(ClickEvent event) {
        event.preventDefault();
        event.stopPropagation();
        fireEvent(CommonEvent.closeEvent(null));
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(900, 480);
    }

    public void setMessage(String altText) {
        lbMessage.setText(altText);
    }

    @Override
    public void onResize() {
        root.onResize();
    }

    public void setRenameFlag(boolean renameFlag) {
        this.renameFlag = renameFlag;
        if (renameFlag) {
            setData("rename", "1");
        }
    }

    interface MultiFileUploaderUiBinder extends UiBinder<DockLayoutPanel, MultiFileUploader> {
    }
}
