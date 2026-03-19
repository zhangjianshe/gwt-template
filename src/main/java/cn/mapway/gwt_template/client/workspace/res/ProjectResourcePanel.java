package cn.mapway.gwt_template.client.workspace.res;

import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.workspace.view.FilePreview;
import cn.mapway.gwt_template.shared.db.DevProjectResourceEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.project.module.ResItem;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.AiAnchor;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;

import java.util.List;

public class ProjectResourcePanel extends CommonEventComposite implements RequiresResize, IData<String> {
    private static final ProjectResourcePanelUiBinder ourUiBinder = GWT.create(ProjectResourcePanelUiBinder.class);
    @UiField
    HTMLPanel naviBar;
    @UiField
    ProjectResourceList list;
    @UiField
    DockLayoutPanel root;
    @UiField
    FilePreview filePreview;
    private String projectId;

    public ProjectResourcePanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @UiHandler("list")
    public void treeCommon(CommonEvent event) {
        if (event.isSelect()) {
            Object data = event.getValue();
            if (data instanceof DevProjectResourceEntity) {
                list.loadRootDir(((DevProjectResourceEntity) data).getId());
            } else if (data instanceof ResItem) {
                list.loadDir(((ResItem) data).getPathName());
            }
        } else if (event.isPath()) {
            NavInfo navInfo = event.getValue();
            renderNavibar(navInfo);
        } else if (event.isView()) {
            NavInfo navInfo = event.getValue();
            CommonPermission permission = CommonPermission.from(navInfo.getResource().getPermission());
            filePreview.enableSave(permission.isSuper() || permission.canUpdate());
            filePreview.preview(navInfo.getResource().getId(), navInfo.getFile());
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
                    list.reload();
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
                list.loadRootDir((String) btnResource.getData());
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
        list.setData(projectId);
    }


    interface ProjectResourcePanelUiBinder extends UiBinder<DockLayoutPanel, ProjectResourcePanel> {
    }
}