package cn.mapway.gwt_template.client.workspace.res;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.widget.file.MultiFileUploader;
import cn.mapway.gwt_template.client.workspace.widget.ActionMenu;
import cn.mapway.gwt_template.client.workspace.widget.ActionMenuKind;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectResourceEntity;
import cn.mapway.gwt_template.shared.rpc.file.FileUtil;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.project.module.ResItem;
import cn.mapway.gwt_template.shared.rpc.project.res.*;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.Colors;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.client.widget.panel.MessagePanel;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import elemental2.promise.IThenable;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.gwt.dom.client.NativeEvent.BUTTON_RIGHT;

public class ProjectResourceList extends CommonEventComposite implements IData<String> {
    private static final ProjectResourceListUiBinder ourUiBinder = GWT.create(ProjectResourceListUiBinder.class);
    @UiField
    VerticalPanel list;
    @UiField
    ScrollPanel scroll;
    NavInfo navInfo = new NavInfo();
    ActionMenu allMenu = new ActionMenu();
    DevProjectResourceEntity actionResource = null;
    ResItem actionItem = null;
    ResourceItem selectedItem = null;
    MouseDownHandler itemDownHandler = new MouseDownHandler() {
        @Override
        public void onMouseDown(MouseDownEvent event) {
            if (event.getNativeButton() == BUTTON_RIGHT) {
                event.preventDefault();
                event.stopPropagation();
                ResourceItem item = (ResourceItem) event.getSource();
                selectItem(item);
                showSourceMenu(item, event);
            }
        }
    };
    private String projectId;
    private String currentResourceId = "";
    private final CommonEventHandler archiveItemHandler = commonEvent -> {
        if (commonEvent.isSelect()) {
            DevProjectResourceEntity data = commonEvent.getValue();
            currentResourceId = data.getId();
            navInfo.setResource(data);
            fireEvent(CommonEvent.selectEvent(data));
        }

    };
    private CommonEventHandler folderFileHandler= commonEvent -> {
        if (commonEvent.isSelect()) {
            ResourceItem source = (ResourceItem) commonEvent.getSource();
            selectItem(source);
            ResItem data = (ResItem) source.getData();
            if (!data.getIsDir()) {
                navInfo.setFile(data.getPathName());
                fireEvent(CommonEvent.viewEvent(navInfo));
            } else {
                loadDir(data.getPathName());
            }
        }
    };

    public ProjectResourceList() {
        initWidget(ourUiBinder.createAndBindUi(this));
        allMenu.addCommonHandler(new CommonEventHandler() {

            @Override
            public void onCommonEvent(CommonEvent event) {
                ActionMenuKind action = event.getValue();
                switch (action) {
                    case AMK_CREATE_RESOURCE: {
                        createResource();
                        break;
                    }
                    case AMK_CREATE_DIRFILE: {
                        createDirFile();
                        break;
                    }
                    case AMK_DELETE_RESOURCE: {
                        confirmDelete(actionResource);
                        break;
                    }
                    case AMK_DELETE_DIRFILE: {
                        confirmDeleteDirFile(actionItem);
                        break;
                    }
                    case AMK_UPLOAD_DIRFILE: {
                        //向当前目录上传文件
                        uploadFileTo(navInfo.relPath);
                        break;
                    }
                }
                allMenu.hide();
            }
        });


        scroll.addDomHandler(event -> {
            event.preventDefault();
            event.stopPropagation();
        }, ContextMenuEvent.getType());
        scroll.addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                if (event.getNativeButton() == BUTTON_RIGHT) {
                    event.stopPropagation();
                    event.preventDefault();
                    showAllMenu(event);
                }
            }
        }, MouseDownEvent.getType());
    }

    private void uploadFileTo(String relPath) {
        Dialog<MultiFileUploader> dialog = MultiFileUploader.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isRefresh()) {
                    loadDir(navInfo.relPath);
                    dialog.hide();
                } else if (event.isClose()) {
                    dialog.hide();
                }
            }
        });
        String path = AppConstant.UPLOAD_PREFIX_PROJECT_RESOURCE + navInfo.getResource().getId() + ":" + ((StringUtil.isBlank(relPath)) ? "/" : relPath);
        dialog.getContent().setPath(path);
        dialog.getContent().setAction(GWT.getHostPageBaseURL() + "/api/v1/project/upload");
        dialog.center();
    }

    private void confirmDeleteDirFile(ResItem actionItem) {
        if (actionItem == null) {
            return;
        }

        String msg = "删除";
        if (actionItem.getIsDir()) {
            msg += "目录 " + StringUtil.extractName(actionItem.getPathName()) + "?";
        } else {
            msg += "文件" + StringUtil.extractName(actionItem.getPathName()) + "?";
        }
        ClientContext.get().confirmDelete(msg).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
            @Override
            public @Nullable IThenable<Object> onInvoke(Void p0) {
                doDeleteDirFile(actionItem);
                return null;
            }
        });
    }

    private void doDeleteDirFile(ResItem actionItem) {
        DeleteProjectDirFileRequest request = new DeleteProjectDirFileRequest();
        request.setResourceId(navInfo.getResource().getId());
        request.setRelativePathName(actionItem.getPathName());
        AppProxy.get().deleteProjectDirFile(request, new AsyncCallback<RpcResult<DeleteProjectDirFileResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<DeleteProjectDirFileResponse> result) {
                if (result.isSuccess()) {
                    loadDir(navInfo.relPath);
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    private void createDirFile() {
        Dialog<CreateResourceDirFilePanel> dialog = CreateResourceDirFilePanel.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isClose()) {
                    dialog.hide();
                } else if (event.isUpdate()) {
                    loadDir(navInfo.relPath);
                    dialog.hide();
                }
            }
        });
        dialog.getContent().setContextData(navInfo.getResource().getId(), navInfo.getRelPath());
        dialog.getContent().init();
        dialog.center();
    }

    private void selectItem(ResourceItem item) {
        if (selectedItem != null) {
            selectedItem.setSelected(false);
            selectedItem = null;
        }
        selectedItem = item;
        if (selectedItem != null) {
            selectedItem.setSelected(true);
        }
    }

    private void confirmDelete(DevProjectResourceEntity resource) {
        if (resource == null) {
            return;
        }
        String msg = "删除资源分类 " + resource.getName() + "?";
        msg += "<p style='color:red;font-weight:bold;font-size:1.2rem;'>注意 这样会删除分类下所有的文件</p>";
        ClientContext.get().confirmDelete(msg).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
            @Override
            public @Nullable IThenable<Object> onInvoke(Void p0) {
                doDeleteResource(resource);
                return null;
            }
        });
    }

    private void doDeleteResource(DevProjectResourceEntity resource) {
        DeleteProjectResourceRequest request = new DeleteProjectResourceRequest();
        request.setResourceId(resource.getId());
        AppProxy.get().deleteProjectResource(request, new AsyncCallback<RpcResult<DeleteProjectResourceResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<DeleteProjectResourceResponse> result) {
                if (result.isSuccess()) {
                    setData(projectId);
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    private void showSourceMenu(ResourceItem item, MouseDownEvent event) {
        allMenu.clear();
        Object data = item.getData();
        boolean hasMenu = false;

        if (data instanceof DevProjectResourceEntity) {
            DevProjectResourceEntity resource = (DevProjectResourceEntity) data;
            actionResource = resource;
            actionItem = null;
            if (ClientContext.get().isCurrentUser(resource.getUserId())) {
                allMenu.addItem("📁 创建档案袋", ActionMenuKind.AMK_CREATE_RESOURCE, true);
                allMenu.addSeparator();
                allMenu.addItem("❌ 删除档案袋: " + resource.getName(), ActionMenuKind.AMK_DELETE_RESOURCE, true);
                hasMenu = true;
            }
        } else if (data instanceof ResItem) {

            CommonPermission permission = CommonPermission.from(navInfo.getResource().getPermission());
            ResItem resItem = (ResItem) data;
            actionResource = null;
            actionItem = resItem;

            if (permission.isSuper() || permission.canCreate()) {
                allMenu.addItem("➕ 创建目录和文件", ActionMenuKind.AMK_CREATE_DIRFILE, true);
                allMenu.addItem("📤 上传文件", ActionMenuKind.AMK_UPLOAD_DIRFILE, true);
                hasMenu = true;
            }

            if (hasMenu) allMenu.addSeparator();

            if (permission.isSuper() || permission.canDelete()) {
                allMenu.addItem("❌ 删除 " + StringUtil.extractName(resItem.getPathName()), ActionMenuKind.AMK_DELETE_DIRFILE, true);
                hasMenu = true;
            }
        }

        if (hasMenu) {
            allMenu.setPopupPosition(event.getClientX() + 3, event.getClientY() + 3);
            allMenu.show();
        }
    }

    private void showAllMenu(MouseDownEvent event) {
        allMenu.clear();
        if (navInfo.getResource() == null) {
            if (ClientContext.get().isCurrentUser(navInfo.getProject().getUserId())) {
                allMenu.addItem("📁 创建档案袋", ActionMenuKind.AMK_CREATE_RESOURCE);
            } else {
                allMenu.addItem("无权限创建", ActionMenuKind.AMK_TIP);
            }
        } else {
            CommonPermission permission = CommonPermission.from(navInfo.getResource().getPermission());
            if (permission.isSuper()) {
                allMenu.addItem("➕ 创建目录和文件", ActionMenuKind.AMK_CREATE_DIRFILE, true);
                allMenu.addItem("📤 上传文件", ActionMenuKind.AMK_UPLOAD_DIRFILE, true);

            } else {
                allMenu.addItem("\uD83D\uDEAB 无权限创建", ActionMenuKind.AMK_TIP);
            }
        }
        allMenu.setPopupPosition(event.getClientX() + 3, event.getClientY() + 3);
        allMenu.show();
    }

    @Override
    public String getData() {
        return projectId;
    }

    @Override
    public void setData(String obj) {
        projectId = obj;
        toUI();
    }

    private void toUI() {
        loadResourceList(projectId);
    }

    public void loadResourceList(String projectId) {
        selectItem(null);
        this.projectId = projectId;
        QueryProjectResourceRequest request = new QueryProjectResourceRequest();
        request.setProjectId(projectId);
        list.clear();
        AppProxy.get().queryProjectResource(request, new AsyncCallback<RpcResult<QueryProjectResourceResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryProjectResourceResponse> result) {
                if (result.isSuccess()) {

                    renderList(result.getData().getResources());
                    navInfo.setProject(result.getData().getProject());
                    navInfo.setResource(null);
                    navInfo.setRelPath("");
                    fireEvent(CommonEvent.pathEvent(navInfo));
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }

        });
    }

    private void renderList(List<DevProjectResourceEntity> resources) {
        list.clear();
        for (DevProjectResourceEntity resource : resources) {
            ResourceItem item = new ResourceItem();
            item.setData(resource);

            CommonPermission permission = CommonPermission.from(resource.getPermission());
            if (!(permission.isSuper() || permission.canRead())) {
                continue;
            }

            if (permission.isSuper()) {
                FontIcon config = new FontIcon();
                config.setPushButton(true);
                config.setIconUnicode(Fonts.UI_CONFIG);
                config.setData(resource);
                config.getElement().getStyle().setProperty("justifySelf", "flex-end");
                config.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        event.stopPropagation();
                        event.preventDefault();
                        showConfig(resource);
                    }
                });
                item.setValue(Fonts.ARCHIVE, resource.getName(), config);
            } else {
                item.setValue(Fonts.ARCHIVE, resource.getName(), "");
            }
            item.updateNameColor(resource.getName(), resource.getColor());
            item.addCommonHandler(archiveItemHandler);
            item.addDomHandler(itemDownHandler, MouseDownEvent.getType());

            list.add(item);
        }
        if (resources.isEmpty()) {
            MessagePanel messagePanel = new MessagePanel();
            messagePanel.setHeight("200px");
            messagePanel.setText("右键菜单创建新的资源分类");
            list.add(messagePanel);
        } else if (list.getWidgetCount() == 0) {
            MessagePanel messagePanel = new MessagePanel();
            messagePanel.setHeight("200px");
            messagePanel.setText("您还没有授权查看的项目资源,联系项目管理员查看");
            list.add(messagePanel);
        }
    }

    private void showConfig(DevProjectResourceEntity resource) {
        Popup<ResourceConfigPanel> dialog = ResourceConfigPanel.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isClose()) {
                    dialog.hide();
                } else if (event.isUpdate()) {
                    DevProjectResourceEntity data = event.getValue();
                    updateItem(data);
                }
            }
        });
        dialog.center();
        dialog.getContent().setData(resource);
    }

    private void updateItem(DevProjectResourceEntity data) {
        for (int i = 0; i < list.getWidgetCount(); i++) {
            Widget widget = list.getWidget(i);
            if (widget instanceof ResourceItem) {
                ResourceItem item = (ResourceItem) widget;
                Object object = item.getData();
                if (object instanceof DevProjectResourceEntity) {
                    DevProjectResourceEntity data2 = (DevProjectResourceEntity) object;
                    if (data2.getId().equals(data.getId())) {
                        data2.setName(data.getName());
                        data2.setColor(data.getColor());
                        item.updateNameColor(data2.getName(), data2.getColor());
                        break;
                    }
                }
            }
        }
    }

    public void reload() {
        loadResourceList(projectId);
    }

    public void loadDir(String relPath) {
        if (StringUtil.isBlank(relPath)) {
            relPath = "";
        }
        QueryProjectDirRequest request = new QueryProjectDirRequest();
        request.setResourceId(currentResourceId);
        request.setPath(relPath);
        AppProxy.get().queryProjectDir(request, new AsyncCallback<RpcResult<QueryProjectDirResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryProjectDirResponse> result) {
                if (result.isSuccess()) {
                    list.clear();
                    Collections.sort(result.getData().getResources(), new Comparator<ResItem>() {
                        @Override
                        public int compare(ResItem o1, ResItem o2) {
                            if (o1.getIsDir()) {
                                if (o2.getIsDir()) {
                                    return o1.getPathName().compareTo(o2.getPathName());
                                }
                                return -1;
                            } else {
                                if (!o2.getIsDir()) {
                                    return o1.getPathName().compareTo(o2.getPathName());
                                }
                                return 1;
                            }
                        }
                    });
                    for (ResItem resItem : result.getData().getResources()) {
                        ResourceItem item = new ResourceItem();
                        item.setData(resItem);
                        String suffix = StringUtil.suffix(resItem.getPathName());
                        item.setValue(resItem.getIsDir() ? Fonts.FOLDER : FileUtil.iconFromSuffix(suffix),
                                StringUtil.extractName(resItem.getPathName()), StringUtil.formatFileSize(resItem.getFileSize().longValue()));
                        list.add(item);
                        item.addDomHandler(itemDownHandler, MouseDownEvent.getType());
                        item.addCommonHandler(folderFileHandler);
                    }
                    if (result.getData().getResources().isEmpty()) {
                        MessagePanel messagePanel = new MessagePanel();
                        messagePanel.setText("该目录内目前没有数据，右键上传.");
                        messagePanel.setHeight("200px");
                        list.add(messagePanel);
                    }
                    navInfo.setRelPath(result.getData().getRequestPath());
                    fireEvent(CommonEvent.pathEvent(navInfo));
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    public void loadRootDir(String resourceId) {
        currentResourceId = resourceId;
        loadDir("");
    }

    public void createResource() {
        ClientContext.get().input("输入资源目录", "资源目录", "", "", new Callback() {
            @Override
            public void onFailure(Object reason) {

            }

            @Override
            public void onSuccess(Object result) {
                doCreateResource((String) result);
            }
        });
    }

    private void doCreateResource(String resName) {

        UpdateProjectResourceRequest request = new UpdateProjectResourceRequest();
        DevProjectResourceEntity resource = new DevProjectResourceEntity();
        resource.setProjectId(projectId);
        resource.setName(resName);
        resource.setColor(Colors.randomColor());
        request.setResource(resource);
        AppProxy.get().updateProjectResource(request, new AsyncCallback<RpcResult<UpdateProjectResourceResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateProjectResourceResponse> result) {
                if (result.isSuccess()) {
                    setData(projectId);
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    interface ProjectResourceListUiBinder extends UiBinder<DockLayoutPanel, ProjectResourceList> {
    }



}