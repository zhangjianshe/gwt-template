package cn.mapway.gwt_template.client.workspace.view;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.workspace.res.viewer.HtmlViewer;
import cn.mapway.gwt_template.client.workspace.res.viewer.ImageViewer;
import cn.mapway.gwt_template.client.workspace.res.viewer.InfoViewer;
import cn.mapway.gwt_template.client.workspace.res.viewer.TextEditViewer;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.file.EditableFileSuffix;
import cn.mapway.gwt_template.shared.rpc.file.ImageFileSuffix;
import cn.mapway.gwt_template.shared.rpc.project.ViewAttachmentFileRequest;
import cn.mapway.gwt_template.shared.rpc.project.ViewAttachmentFileResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.PreviewData;
import cn.mapway.gwt_template.shared.rpc.project.res.ViewProjectFileRequest;
import cn.mapway.gwt_template.shared.rpc.project.res.ViewProjectFileResponse;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.Header;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.http.client.URL;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

import java.util.Objects;

/**
 * 文件预览操作
 */
public class FilePreview extends CommonEventComposite implements RequiresResize {
    private static final FilePreviewUiBinder ourUiBinder = GWT.create(FilePreviewUiBinder.class);


    @UiField
    HorizontalPanel toolBar;
    @UiField
    Header lbName;
    @UiField
    LayoutPanel contentPanel;
    @UiField
    DockLayoutPanel root;
    Widget currentWidget = null;
    InfoViewer infoViewer;
    TextEditViewer textEditViewer;
    boolean enableSave = false;
    ImageViewer imageViewer;
    Frame frameViewer;
    HtmlViewer htmlViewer;

    public FilePreview() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public void enableSave(boolean enable) {
        this.enableSave = enable;
    }

    /**
     * 预览附件信息
     *
     * @param taskId   任务ID
     * @param fileName 文件名称
     */
    public void previewAttachment(String taskId, String fileName) {
        ViewAttachmentFileRequest request = new ViewAttachmentFileRequest();
        request.setTaskId(taskId);
        request.setRelPathName(fileName);
        lbName.setText(StringUtil.extractName(fileName));
        AppProxy.get().viewAttachmentFile(request, new AsyncCallback<RpcResult<ViewAttachmentFileResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                switchMessage(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<ViewAttachmentFileResponse> result) {
                if (result.isSuccess()) {
                    toolBar.clear();
                    switchView(result.getData().getPreviewData());
                } else {
                    switchMessage(result.getMessage());
                }
            }
        });
    }

    public void preview(String resourceId, String fileName) {
        ViewProjectFileRequest request = new ViewProjectFileRequest();
        request.setResourceId(resourceId);
        request.setRelPathName(fileName);
        lbName.setText(StringUtil.extractName(fileName));
        AppProxy.get().viewProjectFile(request, new AsyncCallback<RpcResult<ViewProjectFileResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                switchMessage(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<ViewProjectFileResponse> result) {
                if (result.isSuccess()) {
                    toolBar.clear();
                    switchView(result.getData().getPreviewData());
                } else {
                    switchMessage(result.getMessage());
                }
            }
        });
    }

    private void switchEditor(PreviewData response, EditableFileSuffix mode) {
        if (textEditViewer == null) {
            textEditViewer = new TextEditViewer();
        }
        if (!Objects.equals(currentWidget, textEditViewer)) {
            contentPanel.clear();
            currentWidget = textEditViewer;
            contentPanel.add(textEditViewer);
        }
        toolBar.add(textEditViewer.getTools());
        textEditViewer.enableSave(enableSave);
        textEditViewer.setEditorData(response, mode);
    }

    private void switchView(PreviewData data) {
        lbName.setText(StringUtil.extractName(data.getFileName()));

        if (AppConstant.CANGLING_MIME_TYPE.equals(data.getMimeType())) {
            String fileName = data.getFileName();

            // 1. 先进行标准路径片段编码 (会将 / 变成 %2F)
            String encodedPath = URL.encodePathSegment(fileName);

            // 2. 将 %2F 还原为 /，这样 URL 就能保持层级结构，同时文件名中的特殊字符（如空格、#）已被安全编码
            encodedPath = encodedPath.replace("%2F", "/");

            // 3. 拼接基础路径
            String baseUrl = "/api/v1/project/file/" + data.getResourceId();

            // 确保 baseUrl 和 encodedPath 之间只有一个斜杠
            String url;
            if (encodedPath.startsWith("/")) {
                url = baseUrl + encodedPath;
            } else {
                url = baseUrl + "/" + encodedPath;
            }

            switchHtml(data, url);
            return;
        }
        if (AppConstant.CANGLING_MIME_FRAME.equals(data.getMimeType())) {
            switchFrameViewer(data);
            return;
        }

        String suffix = StringUtil.suffix(data.getFileName()).toLowerCase();
        EditableFileSuffix editableFileSuffix = EditableFileSuffix.fromSuffix(suffix);
        if (editableFileSuffix != EditableFileSuffix.NONE) {
            switchEditor(data, editableFileSuffix);
        } else if (ImageFileSuffix.fromSuffix(suffix) != ImageFileSuffix.NONE) {
            switchImageView(data);
        } else {
            switchMessage("不支持" + data.getMimeType());
        }
    }

    private void switchHtml(PreviewData data, String url) {
        if (htmlViewer == null) {
            htmlViewer = new HtmlViewer();
        }
        if (!Objects.equals(currentWidget, htmlViewer)) {
            contentPanel.clear();
            currentWidget = htmlViewer;
            contentPanel.add(htmlViewer);
        }
        toolBar.clear();
        toolBar.add(htmlViewer.getTools());
        htmlViewer.setHtml(data.getBody(), url);
    }

    private void switchFrameViewer(PreviewData data) {
        if (frameViewer == null) {
            frameViewer = new Frame();
            frameViewer.setWidth("100%");
            frameViewer.setHeight("100%");
        }

        // Always clear and re-add or ensure constraints are set
        // to force LayoutPanel to treat it as a full-size child
        if (!Objects.equals(currentWidget, frameViewer)) {
            contentPanel.clear();
            currentWidget = frameViewer;
            contentPanel.add(frameViewer);
            // These MUST be set for the widget to expand in a LayoutPanel
            contentPanel.setWidgetTopBottom(frameViewer, 0, Style.Unit.PX, 0, Style.Unit.PX);
            contentPanel.setWidgetLeftRight(frameViewer, 0, Style.Unit.PX, 0, Style.Unit.PX);
            // This forces the LayoutPanel to recalculate child sizes immediately
            contentPanel.forceLayout();

        }


        frameViewer.setWidth("100%");
        frameViewer.setHeight("100%");

        // Set the URL after the layout is triggered
        frameViewer.setUrl(data.getBody());
    }

    private void switchImageView(PreviewData data) {
        if (imageViewer == null) {
            imageViewer = new ImageViewer();
        }
        if (!Objects.equals(currentWidget, imageViewer)) {
            contentPanel.clear();
            currentWidget = imageViewer;
            contentPanel.add(imageViewer);
        }
        imageViewer.setData(data);
    }

    private void switchMessage(String message) {
        toolBar.clear();
        switchMessage("提示", message);
    }

    private void switchMessage(String header, String message) {
        if (infoViewer == null) {
            infoViewer = new InfoViewer();
        }

        if (!Objects.equals(currentWidget, infoViewer)) {
            contentPanel.clear();
            contentPanel.add(infoViewer);
            currentWidget = infoViewer;
        }
        infoViewer.setMessage(header, message);
    }

    @Override
    public void onResize() {
        root.onResize();
    }

    public void previewEmpty() {
        lbName.setText("");
        switchMessage("", "附件预览");
    }

    interface FilePreviewUiBinder extends UiBinder<DockLayoutPanel, FilePreview> {
    }
}