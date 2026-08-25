package cn.mapway.gwt_template.client.workspace.view;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.workspace.res.viewer.HtmlViewer;
import cn.mapway.gwt_template.client.workspace.res.viewer.ImageViewer;
import cn.mapway.gwt_template.client.workspace.res.viewer.InfoViewer;
import cn.mapway.gwt_template.client.workspace.res.viewer.TextEditViewer;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.docker.ReadDockerAppResDataRequest;
import cn.mapway.gwt_template.shared.rpc.docker.ReadDockerAppResDataResponse;
import cn.mapway.gwt_template.shared.rpc.docker.WriteDockerAppResDataRequest;
import cn.mapway.gwt_template.shared.rpc.docker.WriteDockerAppResDataResponse;
import cn.mapway.gwt_template.shared.rpc.file.EditableFileSuffix;
import cn.mapway.gwt_template.shared.rpc.file.ImageFileSuffix;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectFileRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectFileResponse;
import cn.mapway.gwt_template.shared.rpc.project.ViewAttachmentFileRequest;
import cn.mapway.gwt_template.shared.rpc.project.ViewAttachmentFileResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.PreviewData;
import cn.mapway.gwt_template.shared.rpc.project.res.ViewProjectFileRequest;
import cn.mapway.gwt_template.shared.rpc.project.res.ViewProjectFileResponse;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.Header;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.http.client.URL;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import elemental2.dom.CSSProperties;
import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;

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
    @UiField
    Label lbLink;
    @UiField
    HTML btnCopy;
    Widget currentWidget = null;
    InfoViewer infoViewer;
    TextEditViewer textEditViewer;
    boolean enableSave = false;
    ImageViewer imageViewer;
    Frame frameViewer;
    HtmlViewer htmlViewer;
    PreviewType currentPreviewKind = PreviewType.NONE;
    PreviewData previewData;
    private final CommonEventHandler txtEditorHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent event) {
            if (event.isSave()) {
                switch (currentPreviewKind) {
                    case PROJECT_FILE: {
                        UpdateProjectFileRequest request = new UpdateProjectFileRequest();
                        request.setBody(event.getValue());
                        request.setResourceId(previewData.getResourceId());
                        request.setFilePathName(previewData.getFileName());
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
                        break;
                    }
                    case DOCKER_APP_FILE: {
                        WriteDockerAppResDataRequest request = new WriteDockerAppResDataRequest();
                        request.setDockerAppId(previewData.getResourceId());
                        request.setBody(event.getValue());
                        request.setFilePathName(previewData.getFileName());
                        AppProxy.get().writeDockerAppResData(request, new AsyncCallback<RpcResult<WriteDockerAppResDataResponse>>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                ClientContext.get().toast(0, 0, caught.getMessage());
                            }

                            @Override
                            public void onSuccess(RpcResult<WriteDockerAppResDataResponse> result) {
                                if (result.isSuccess()) {
                                    ClientContext.get().toast(0, 0, "保存成功");
                                } else {
                                    ClientContext.get().toast(0, 0, result.getMessage());
                                }
                            }
                        });
                        break;
                    }
                    case NONE:
                    case TASK_ATTACHMENT:
                    default:
                }

            }
        }
    };

    public FilePreview() {
        initWidget(ourUiBinder.createAndBindUi(this));
        initCopyHandler();
    }

    // 绑定原生的 document.execCommand
    @JsMethod(namespace = JsPackage.GLOBAL, name = "document.execCommand")
    private static native boolean execCommand(String command);

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
        currentPreviewKind = PreviewType.TASK_ATTACHMENT;
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

    public void previewDockerAppRes(String appId, String fileName) {
        currentPreviewKind = PreviewType.DOCKER_APP_FILE;
        ReadDockerAppResDataRequest request = new ReadDockerAppResDataRequest();
        request.setAppId(appId);
        request.setFilePathName(fileName);
        lbName.setText(StringUtil.extractName(fileName));
        AppProxy.get().readDockerAppResData(request, new AsyncCallback<RpcResult<ReadDockerAppResDataResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                switchMessage(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<ReadDockerAppResDataResponse> result) {
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
        currentPreviewKind = PreviewType.PROJECT_FILE;
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
            textEditViewer.addCommonHandler(txtEditorHandler);
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

    // 初始化时绑定点击事件
    private void initCopyHandler() {
        btnCopy.addClickHandler(event -> {
            String urlToCopy = lbLink.getText();
            if (StringUtil.isNotBlank(urlToCopy)) {
                copyToClipboard(urlToCopy);
            }
        });
    }

    /**
     * 使用 Elemental2 进行剪贴板复制（纯纯的 Java 代码，无需 JSNI）
     */
    public void copyToClipboard(String text) {
        // 1. 强制焦点切回当前 window
        DomGlobal.window.focus();

        // 2. 判断 clipboard API 是否支持
        if (DomGlobal.navigator.clipboard != null) {
            DomGlobal.navigator.clipboard.writeText(text).then(p -> {
                ClientContext.get().toast(1, 0, "链接已复制到剪贴板");
                return null;
            }).catch_(err -> {
                // 如果现代 API 失败（如 Document is not focused），降级到 fallback
                fallbackCopyText(text);
                return null;
            });
        } else {
            // 3. 旧版浏览器 / HTTP 环境降级处理
            fallbackCopyText(text);
        }
    }

    /**
     * Elemental2 实现的 ExecCommand 降级方案
     */
    private void fallbackCopyText(String text) {
        try {
            // 使用 Elemental2 动态创建 HTMLTextAreaElement
            elemental2.dom.HTMLTextAreaElement textArea =
                    (elemental2.dom.HTMLTextAreaElement) DomGlobal.document.createElement("textarea");


            textArea.value = text;
            textArea.style.top = "0";
            textArea.style.left = "0";
            textArea.style.position = "fixed";
            textArea.style.opacity = CSSProperties.OpacityUnionType.of("0");

            DomGlobal.document.body.appendChild(textArea);
            textArea.focus();
            textArea.select();

            boolean successful = execCommand("copy");
            DomGlobal.document.body.removeChild(textArea);

            if (successful) {
                ClientContext.get().toast(1, 0, "链接已复制到剪贴板");
            } else {
                ClientContext.get().toast(0, 0, "复制失败，请手动选中复制");
            }
        } catch (Exception e) {
            ClientContext.get().toast(0, 0, "复制失败: " + e.getMessage());
        }
    }

    private void switchView(PreviewData data) {
        this.previewData = data;
        lbName.setText(StringUtil.extractName(data.getFileName()));

        String fileName = data.getFileName();
        // 1. 先进行标准路径片段编码 (会将 / 变成 %2F)
        String encodedPath = URL.encodePathSegment(fileName);
        // 2. 将 %2F 还原为 /，这样 URL 就能保持层级结构，同时文件名中的特殊字符（如空格、#）已被安全编码
        encodedPath = encodedPath.replace("%2F", "/");
        // 3. 拼接基础路径
        String baseUrl = GWT.getHostPageBaseURL() + data.getUrl();
        // 确保 baseUrl 和 encodedPath 之间只有一个斜杠
        String url;
        if (encodedPath.startsWith("/")) {
            url = baseUrl + encodedPath;
        } else {
            url = baseUrl + "/" + encodedPath;
        }
        lbLink.setText(url);
        lbLink.setTitle(url); // 鼠标悬停显示完整链接
        Style linkStyle = lbLink.getElement().getStyle();
        linkStyle.setProperty("maxWidth", "400px");
        linkStyle.setProperty("overflow", "hidden");
        linkStyle.setProperty("textOverflow", "ellipsis");
        linkStyle.setProperty("whiteSpace", "nowrap");
        linkStyle.setProperty("display", "inline-block");

        if (AppConstant.CANGLING_MIME_TYPE.equals(data.getMimeType())) {
            switchHtml(data, url);
            return;
        }
        if (AppConstant.CANGLING_MIME_FRAME.equals(data.getMimeType())) {
            switchFrameViewer(data);
            return;
        }

        String suffix = data.getSuffixName().toLowerCase();
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

    public void previewEmpty(String message) {
        lbName.setText("");
        if (StringUtil.isNotBlank(message)) {
            message = "附件预览";
        }
        switchMessage("", message);
    }

    interface FilePreviewUiBinder extends UiBinder<DockLayoutPanel, FilePreview> {
    }
}