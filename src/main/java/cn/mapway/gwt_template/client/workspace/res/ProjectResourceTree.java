package cn.mapway.gwt_template.client.workspace.res;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DevProjectResourceEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.ResItem;
import cn.mapway.gwt_template.shared.rpc.project.res.QueryProjectDirRequest;
import cn.mapway.gwt_template.shared.rpc.project.res.QueryProjectDirResponse;
import cn.mapway.gwt_template.shared.rpc.project.res.QueryProjectResourceRequest;
import cn.mapway.gwt_template.shared.rpc.project.res.QueryProjectResourceResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.FontIcon;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.client.widget.panel.MessagePanel;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.HasCommonHandlers;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.List;

public class ProjectResourceTree extends VerticalPanel implements HasCommonHandlers {
    String projectId;
    NavInfo navInfo = new NavInfo();
    private String currentResourceId = "";

    public ProjectResourceTree() {
        setWidth("100%");
    }

    public void loadResourceList(String projectId) {
        this.projectId = projectId;
        QueryProjectResourceRequest request = new QueryProjectResourceRequest();
        request.setProjectId(projectId);
        clear();
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
        clear();
        for (DevProjectResourceEntity resource : resources) {
            ResourceItem item = new ResourceItem();
            item.setData(resource);
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
            item.setValue(Fonts.FOLDER, resource.getName(), config);
            item.addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {

                    DevProjectResourceEntity data = (DevProjectResourceEntity) item.getData();
                    currentResourceId = data.getId();
                    navInfo.setResource(data);
                    fireEvent(CommonEvent.selectEvent(resource));
                }
            }, ClickEvent.getType());
            add(item);
        }
        if (resources.isEmpty()) {
            MessagePanel messagePanel = new MessagePanel();
            messagePanel.setHeight("200px");
            messagePanel.setText("没有资源,请添加");
            add(messagePanel);
        }
    }

    private void showConfig(DevProjectResourceEntity resource) {
        Popup<ResourceConfigPanel> dialog = ResourceConfigPanel.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isClose()) {
                    dialog.hide();
                }
            }
        });
        dialog.center();
        dialog.getContent().setData(resource);
    }

    @Override
    public HandlerRegistration addCommonHandler(CommonEventHandler handler) {
        return addHandler(handler, CommonEvent.TYPE);
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
                    clear();
                    for (ResItem resItem : result.getData().getResources()) {
                        ResourceItem item = new ResourceItem();
                        item.setData(resItem);
                        item.setValue(resItem.getIsDir() ? Fonts.FOLDER : Fonts.FILE,
                                StringUtil.extractName(resItem.getPathName()), StringUtil.formatFileSize(resItem.getFileSize().longValue()));
                        add(item);
                    }
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
}
