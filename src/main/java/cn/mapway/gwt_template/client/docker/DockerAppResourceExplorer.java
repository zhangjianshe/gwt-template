package cn.mapway.gwt_template.client.docker;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.widget.file.MultiFileUploader;
import cn.mapway.gwt_template.client.workspace.view.FilePreview;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DockerAppEntity;
import cn.mapway.gwt_template.shared.rpc.docker.DeleteDirFileRequest;
import cn.mapway.gwt_template.shared.rpc.docker.DeleteDirFileResponse;
import cn.mapway.gwt_template.shared.rpc.docker.QueryDockerAppDirRequest;
import cn.mapway.gwt_template.shared.rpc.docker.QueryDockerAppDirResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.ResItem;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.panel.MessagePanel;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import elemental2.promise.IThenable;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DockerAppResourceExplorer extends CommonEventComposite implements IData<DockerAppEntity> {
    private static final DockerAppResourceExplorerUiBinder ourUiBinder = GWT.create(DockerAppResourceExplorerUiBinder.class);

    @UiField
    FilePreview filePreview;
    @UiField
    HTMLPanel list;
    @UiField
    HTMLPanel pathBar;
    @UiField
    SStyle style;
    @UiField
    FontIcon btnUploader;
    @UiField
    FontIcon btnDelete;

    String currentPath = "";
    FileItem selectedFile = null;
    private DockerAppEntity appEntity;

    public DockerAppResourceExplorer() {
        initWidget(ourUiBinder.createAndBindUi(this));
        btnUploader.setIconUnicode(Fonts.UPLOAD_SMALL);
        String btnStyle = AppResource.INSTANCE.styles().toolButton();
        btnUploader.addStyleName(btnStyle);
        btnDelete.setIconUnicode(Fonts.DELETE);
        btnDelete.addStyleName(btnStyle);
    }

    @Override
    public DockerAppEntity getData() {
        return appEntity;
    }

    @Override
    public void setData(DockerAppEntity obj) {
        appEntity = obj;
        toUI();
    }

    private void toUI() {
        loadPath("/");
        boolean canOperate = ClientContext.get().isAssignRole(AppConstant.ROLE_DOCKER_APP_MANAGER);
        filePreview.enableSave(canOperate);
        btnUploader.setEnabled(canOperate);
        btnDelete.setEnabled(canOperate);
    }

    private void loadPath(String path) {
        path = normalizePath(path);
        QueryDockerAppDirRequest request = new QueryDockerAppDirRequest();
        request.setDockerAppId(appEntity.getId());
        request.setPath(path);

        AppProxy.get().queryDockerAppDir(request, new AsyncCallback<RpcResult<QueryDockerAppDirResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryDockerAppDirResponse> result) {
                if (result.isSuccess()) {
                    list.clear();
                    if (selectedFile != null) {
                        selectedFile = null;
                    }

                    if (result.getData().getFiles().isEmpty()) {
                        MessagePanel messagePanel = new MessagePanel().setText("该目录下没有子目录和文件");
                        messagePanel.setHeight("150px");
                        list.add(messagePanel);
                    } else {
                        for (ResItem item : result.getData().getFiles()) {
                            FileItem fileItem = new FileItem();
                            fileItem.setData(item);
                            fileItem.addDomHandler(itemClicked, ClickEvent.getType());
                            list.add(fileItem);
                        }
                    }
                    renderPath(result.getData().getPath());
                    btnDelete.setEnabled(false);
                    filePreview.previewEmpty("");
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    /**
     * 渲染面包屑路径栏 (格式如: /ABC/def/deede)
     */
    private void renderPath(String path) {
        currentPath = normalizePath(path);
        pathBar.clear();


        // 1. 拆分路径层级
        String[] parts = currentPath.split("/");
        List<String> validParts = new ArrayList<>();
        for (String part : parts) {
            if (StringUtil.isNotBlank(part)) {
                validParts.add(part);
            }
        }

        // 2. 处理根节点
        if (validParts.isEmpty()) {
            // 当前正处于根目录，根节点不可点击，以加粗 Label 形式展示
            Label rootLabel = new Label("根目录");
            rootLabel.setStyleName(style.currentPathItem());
            pathBar.add(rootLabel);
            return;
        } else {
            // 当前处于子目录，根节点展现为可点击超链接
            Anchor rootBtn = new Anchor("根目录");
            rootBtn.setStyleName(style.link());
            rootBtn.setTitle("返回根目录");
            rootBtn.addClickHandler(e -> loadPath("/"));
            pathBar.add(rootBtn);
        }

        // 3. 循环渲染层级路径 (确保 accumulatedPath 始终以 / 开头)
        StringBuilder accumulatedPath = new StringBuilder();

        for (int i = 0; i < validParts.size(); i++) {
            String part = validParts.get(i);

            // 修复 Bug A：确保路径拼接始终为绝对路径格式 /part1/part2
            accumulatedPath.append("/").append(part);
            final String targetPath = accumulatedPath.toString();

            // 统一层级分隔符 " / "
            HTML separator = new HTML("<span style='color: #bfbfbf; margin: 0 4px; user-select: none;'>/</span>");
            pathBar.add(separator);

            if (i == validParts.size() - 1) {
                // 当前所在末尾目录：普通文本加粗展示，不绑定点击事件
                Label currentItem = new Label(part);
                currentItem.setStyleName(style.currentPathItem());
                pathBar.add(currentItem);
            } else {
                // 中间父级目录：展示为可点击链接
                Anchor itemBtn = new Anchor(part);
                itemBtn.setStyleName(style.link());
                itemBtn.addClickHandler(e -> loadPath(targetPath));
                pathBar.add(itemBtn);
            }
        }
    }

    /**
     * 规范化路径：去除连续多余斜杠并去除末尾斜杠
     */
    private String normalizePath(String path) {
        if (StringUtil.isBlank(path)) {
            return "/";
        }
        path = path.replaceAll("/+", "/");
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    @UiHandler("btnUploader")
    public void btnUploaderClick(ClickEvent event) {
        //向目录中上传文件
        Dialog<MultiFileUploader> dialog = MultiFileUploader.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isRefresh()) {
                    loadPath(currentPath);
                    dialog.hide();
                } else if (event.isClose()) {
                    dialog.hide();
                }
            }
        });
        String path = AppConstant.UPLOAD_DOCKER_APP_RESOURCE + appEntity.getId() + ":" + ((StringUtil.isBlank(currentPath)) ? "/" : currentPath);
        dialog.getContent().setPath(path);
        dialog.getContent().setAction(GWT.getHostPageBaseURL() + "/api/v1/project/upload");
        dialog.center();
    }

    @UiHandler("btnDelete")
    public void btnDeleteClick(ClickEvent event) {
        if (btnUploader.getEnabled() && selectedFile != null) {
            ResItem data = selectedFile.getData();
            String msg = "删除文件" + data.getPathName();
            ClientContext.get().confirmDelete(msg).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
                @Override
                public @Nullable IThenable<Object> onInvoke(Void p0) {
                    doDelete(selectedFile);
                    return null;
                }
            });

        }
    }


    private void doDelete(FileItem selectedFile) {
        DeleteDirFileRequest request = new DeleteDirFileRequest();
        request.setDockerAppId(appEntity.getId());
        request.setFilePathName(StringUtil.concatPath(currentPath, selectedFile.getData().getPathName()));
        AppProxy.get().deleteDirFile(request, new AsyncCallback<RpcResult<DeleteDirFileResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<DeleteDirFileResponse> result) {
                if (result.isSuccess()) {
                    loadPath(currentPath);
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    interface SStyle extends CssResource {
        String top();

        String list();

        String pathbar();

        String currentPathItem();

        String link();
    }

    interface DockerAppResourceExplorerUiBinder extends UiBinder<DockLayoutPanel, DockerAppResourceExplorer> {
    }

    private final ClickHandler itemClicked = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            FileItem fileItem = (FileItem) event.getSource();
            ResItem data = fileItem.getData();
            if (data.getIsDir()) {
                String path = normalizePath(currentPath + "/" + data.getPathName());
                loadPath(path);
            } else {
                if (selectedFile != null) {
                    selectedFile.setSelect(false);
                }
                selectedFile = fileItem;
                selectedFile.setSelect(true);

                String targetFilePath = normalizePath(currentPath + "/" + data.getPathName());
                filePreview.previewDockerAppRes(appEntity.getId(), targetFilePath);
                btnDelete.setData(selectedFile);
                if (btnUploader.getEnabled()) {
                    btnDelete.setEnabled(true);
                }
            }
        }
    };


}