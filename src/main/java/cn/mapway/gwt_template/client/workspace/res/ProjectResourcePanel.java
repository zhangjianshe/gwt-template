package cn.mapway.gwt_template.client.workspace.res;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DevProjectResourceEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.ResItem;
import cn.mapway.gwt_template.shared.rpc.project.res.UpdateProjectResourceRequest;
import cn.mapway.gwt_template.shared.rpc.project.res.UpdateProjectResourceResponse;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.Colors;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.AiAnchor;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

import java.util.List;

public class ProjectResourcePanel extends CommonEventComposite implements RequiresResize, IData<String> {
    private static final ProjectResourcePanelUiBinder ourUiBinder = GWT.create(ProjectResourcePanelUiBinder.class);
    @UiField
    HTMLPanel naviBar;
    @UiField
    ProjectResourceTree tree;
    @UiField
    DockLayoutPanel root;
    @UiField
    Button btnCreateResource;
    private String projectId;

    public ProjectResourcePanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @UiHandler("tree")
    public void treeCommon(CommonEvent event) {
        if (event.isSelect()) {
            Object data = event.getValue();
            if (data instanceof DevProjectResourceEntity) {
                // load root
                tree.loadRootDir(((DevProjectResourceEntity) data).getId());
            } else if (data instanceof ResItem) {
                // load dir
                tree.loadDir(((ResItem) data).getPathName());
            }
        } else if (event.isPath()) {
            NavInfo navInfo = event.getValue();
            renderNavibar(navInfo);
        }
    }

    private void renderNavibar(NavInfo navInfo) {
        naviBar.clear();
        if (navInfo.getProject() == null) {
            naviBar.add(new Label("select a project"));
            return;
        } else {
            AiAnchor btnProjectHome = new AiAnchor();
            btnProjectHome.addStyleName(AppResource.INSTANCE.styles().boldLink());
            btnProjectHome.setText(navInfo.getProject().getName());
            btnProjectHome.setData(navInfo.getProject());
            btnProjectHome.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    tree.reload();
                }
            });
            naviBar.add(btnProjectHome);
        }
        if (navInfo.getResource() == null) {
            return;
        }
        naviBar.add(new Label(">"));
        AiAnchor btnResource = new AiAnchor();
        btnResource.addStyleName(AppResource.INSTANCE.styles().normalLink());
        btnResource.setText(navInfo.getResource().getName());
        btnResource.setData(navInfo.getResource().getId());
        btnResource.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                tree.loadRootDir((String) btnResource.getData());
            }
        });
        naviBar.add(btnResource);

        if (StringUtil.isNotBlank(navInfo.getRelPath())) {
            String path = navInfo.getRelPath().trim();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            List<String> list = StringUtil.splitIgnoreBlank(path, "/");
            String relPath = "";
            for (String s : list) {
                naviBar.add(new Label(">"));
                relPath += s;
                AiAnchor link = new AiAnchor();
                link.addStyleName(AppResource.INSTANCE.styles().normalLink());
                link.setText(s);
                link.setData(relPath);
                naviBar.add(link);
            }
        }
    }

    @UiHandler("btnCreateResource")
    public void btnCreateResourceClick(ClickEvent event) {
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

    @Override
    public void onResize() {
        root.onResize();
    }

    @Override
    public String getData() {
        return projectId;
    }

    @Override
    public void setData(String projectId) {
        this.projectId = projectId;
        tree.loadResourceList(projectId);
    }


    interface ProjectResourcePanelUiBinder extends UiBinder<DockLayoutPanel, ProjectResourcePanel> {
    }
}