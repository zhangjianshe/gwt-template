package cn.mapway.gwt_template.client.workspace.wiki;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DevProjectPageCommitEntity;
import cn.mapway.gwt_template.shared.db.DevProjectPageEntity;
import cn.mapway.gwt_template.shared.rpc.project.wiki.QueryPageRequest;
import cn.mapway.gwt_template.shared.rpc.project.wiki.QueryPageResponse;
import cn.mapway.gwt_template.shared.rpc.project.wiki.UpdatePageRequest;
import cn.mapway.gwt_template.shared.rpc.project.wiki.UpdatePageResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.IEachElement;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.buttons.PlusButton;
import cn.mapway.ui.client.widget.tree.Tree;
import cn.mapway.ui.client.widget.tree.TreeItem;
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
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

import java.util.List;

public class WikiFrame extends CommonEventComposite implements IData<String> {
    private static final WikiFrameUiBinder ourUiBinder = GWT.create(WikiFrameUiBinder.class);
    @UiField
    Tree pageTree;
    @UiField
    PageEditor pageEditor;
    @UiField
    AiButton btnCreate;
    @UiField
    AiButton btnSave;
    @UiField
    AiButton btnHistory;
    @UiField
    WikiHistoryTimeline commitHistory;
    @UiField
    DockLayoutPanel content;
    @UiField
    ScrollPanel commitPanel;
    private String projectId;
    private final ClickHandler addChildPage = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            PlusButton source = (PlusButton) event.getSource();
            DevProjectPageEntity pageEntity = (DevProjectPageEntity) source.getData();
            confirm(pageEntity);
        }
    };

    public WikiFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    private void confirm(DevProjectPageEntity pageEntity) {
        ClientContext.get().input("输入页面标题", "页面标题", "", "", new Callback() {
            @Override
            public void onFailure(Object reason) {

            }

            @Override
            public void onSuccess(Object result) {
                String title = (String) result;
                doCreatePage(pageEntity, title);
            }
        });
    }

    private void doCreatePage(DevProjectPageEntity parent, String title) {
        DevProjectPageEntity pageEntity = new DevProjectPageEntity();
        pageEntity.setProjectId(this.projectId); // 设置所属项目
        pageEntity.setName(title);

        if (parent != null) {
            pageEntity.setParentId(parent.getId());
        } else {
            pageEntity.setParentId(""); // 根节点
        }
        UpdatePageRequest request = new UpdatePageRequest();
        request.setPage(pageEntity);
        AppProxy.get().updatePage(request, new AsyncCallback<RpcResult<UpdatePageResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdatePageResponse> result) {
                if (result.isSuccess()) {
                    loadPages();
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });

    }

    @Override
    public String getData() {
        return projectId;
    }

    @Override
    public void setData(String s) {
        if (StringUtil.isBlank(s)) {
            pageTree.clear();
            return;
        }
        if (s.equals(projectId)) {
            return;
        }
        projectId = s;
        loadPages();
    }

    private void loadPages() {
        QueryPageRequest request = new QueryPageRequest();
        request.setProjectId(projectId);
        AppProxy.get().queryPage(request, new AsyncCallback<RpcResult<QueryPageResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                btnSave.setEnabled(false);
                btnHistory.setEnabled(false);
                pageTree.setMessage(caught.getMessage(), 160);
            }

            @Override
            public void onSuccess(RpcResult<QueryPageResponse> result) {
                if (result.isSuccess()) {
                    btnSave.setEnabled(true);
                    btnHistory.setEnabled(true);
                    renderPages(result.getData());

                } else {
                    btnSave.setEnabled(false);
                    btnHistory.setEnabled(false);
                    pageTree.setMessage(result.getMessage(), 160);
                }
            }
        });
    }

    private void renderPages(QueryPageResponse data) {
        if (data.getRootPages().isEmpty()) {
            pageTree.setMessage("还没有创建页面", 160);
        } else {
            pageTree.setMessage("", 0);
            recursiveAddItem(null, data.getRootPages());
        }
    }

    private void recursiveAddItem(TreeItem parent, List<DevProjectPageEntity> pages) {
        if (pages == null) return;
        for (DevProjectPageEntity page : pages) {
            // 使用自定义图标或 Emoji (如果有)
            TreeItem item = pageTree.addItem(parent, page.getName(), Fonts.CAOZUORIZHI1);
            item.setData(page);

            // 快捷添加按钮
            PlusButton btn = new PlusButton();
            btn.setTitle("添加子页面");
            btn.setData(page);
            btn.addClickHandler(addChildPage);
            item.appendRightWidget(btn);

            // 递归处理子节点
            if (page.getChildren() != null && !page.getChildren().isEmpty()) {
                recursiveAddItem(item, page.getChildren());
            }
        }
    }

    @UiHandler("pageEditor")
    public void pageEditorCommon(CommonEvent event) {
        if (event.isUpdate()) {
            DevProjectPageEntity page = event.getValue();
            updateListPage(page);
            if (content.getWidgetSize(commitPanel) > 0) {
                commitHistory.loadPageHistory(page.getId());
            }
        }
    }

    private void updateListPage(DevProjectPageEntity page) {
        pageTree.eachItem(new IEachElement<TreeItem>() {
            @Override
            public boolean each(TreeItem treeItem) {
                DevProjectPageEntity oldPage = (DevProjectPageEntity) treeItem.getData();
                if (oldPage.getId().equals(page.getId())) {
                    treeItem.setData(page);
                    treeItem.setText(page.getName());
                    return false;
                }
                return true;
            }
        });
    }

    @UiHandler("pageTree")
    public void pageTreeCommon(CommonEvent event) {
        if (event.isSelect()) {
            TreeItem item = event.getValue();
            DevProjectPageEntity pageEntity = (DevProjectPageEntity) item.getData();
            pageEditor.setData(pageEntity);
        }
    }

    @UiHandler("btnCreate")
    public void btnCreateClick(ClickEvent event) {
        confirm(null);
    }

    @UiHandler("btnSave")
    public void btnSaveClick(ClickEvent event) {
        pageEditor.save();
    }

    @UiHandler("btnHistory")
    public void btnHistoryClick(ClickEvent event) {
        if (content.getWidgetSize(commitPanel) > 0) {
            content.setWidgetSize(commitPanel, 0);
        } else {
            content.setWidgetSize(commitPanel, 300);
            commitHistory.loadPageHistory(pageEditor.pageEntity.getId());
        }
    }

    @UiHandler("commitHistory")
    public void commitHistoryCommon(CommonEvent event) {
        if (event.isSelect()) {
            DevProjectPageCommitEntity commit = event.getValue();
            pageEditor.loadCommit(commit);
        }
    }

    interface WikiFrameUiBinder extends UiBinder<DockLayoutPanel, WikiFrame> {
    }
}