package cn.mapway.gwt_template.client.workspace.wiki;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.workspace.wiki.component.WikiContext;
import cn.mapway.gwt_template.shared.db.DevProjectPageCommitEntity;
import cn.mapway.gwt_template.shared.db.DevProjectPageEntity;
import cn.mapway.gwt_template.shared.db.DevProjectPageSectionEntity;
import cn.mapway.gwt_template.shared.rpc.project.wiki.QueryPageSectionRequest;
import cn.mapway.gwt_template.shared.rpc.project.wiki.QueryPageSectionResponse;
import cn.mapway.gwt_template.shared.rpc.project.wiki.UpdatePageSectionRequest;
import cn.mapway.gwt_template.shared.rpc.project.wiki.UpdatePageSectionResponse;
import cn.mapway.gwt_template.shared.wiki.WikiComponentManager;
import cn.mapway.gwt_template.shared.wiki.component.IWikiComponent;
import cn.mapway.gwt_template.shared.wiki.component.WikiBaseComponent;
import cn.mapway.gwt_template.shared.wiki.component.WikiPageContext;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;

/**
 * WIKI 页面编辑器
 */
public class PageEditor extends CommonEventComposite implements IData<DevProjectPageEntity> {
    private static final PageEditorUiBinder ourUiBinder = GWT.create(PageEditorUiBinder.class);
    WikiPageContext wikiPageContext;
    List<IWikiComponent> sections;
    @UiField
    FlowPanel page;
    DevProjectPageEntity pageEntity;

    public PageEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
        wikiPageContext = new WikiPageContext();
        sections = new ArrayList<>();
    }

    private void updateSection(DevProjectPageSectionEntity section) {
        ArrayList<DevProjectPageSectionEntity> sections = new ArrayList<>();
        sections.add(section);
        doUpdate(sections);
    }

    @Override
    public DevProjectPageEntity getData() {
        return pageEntity;
    }

    @Override
    public void setData(DevProjectPageEntity pageEntity) {
        page.clear();
        this.pageEntity = pageEntity;
        if (pageEntity == null) {
            return;
        }
        loadPage(pageEntity.getId(), "");
    }

    private void loadPage(String pageId, String commitId) {
        QueryPageSectionRequest request = new QueryPageSectionRequest();
        request.setPageId(pageId);
        request.setCommitId(commitId);
        AppProxy.get().queryPageSection(request, new AsyncCallback<RpcResult<QueryPageSectionResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryPageSectionResponse> result) {
                if (result.isSuccess()) {
                    renderDocument(result.getData());
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    private void renderDocument(QueryPageSectionResponse data) {
        page.clear();
        sections.clear();
        wikiPageContext.setPage(pageEntity);
        for (DevProjectPageSectionEntity section : data.getSections()) {
            WikiComponentManager manager = WikiContext.get();
            IWikiComponent component = manager.createComponent(section.getKind());
            component.initComponent(wikiPageContext, section);
            component.addCommonHandler(itemHandler);
            sections.add(component);
            Widget rootWidget = component.getRootWidget();
            page.add(rootWidget);
        }
    }

    /**
     * 保存变更的页面
     */
    public void save() {
        List<DevProjectPageSectionEntity> updatedSectionList = new ArrayList<>();
        for (int i = 0; i < page.getWidgetCount(); i++) {
            Widget widget = page.getWidget(i);
            if (widget instanceof WikiBaseComponent) {
                WikiBaseComponent item = (WikiBaseComponent) widget;
                if (item.isChanged()) {
                    updatedSectionList.add(item.getSection());
                }
            }
        }
        if (!updatedSectionList.isEmpty()) {
            doUpdate(updatedSectionList);
        }
    }

    private void doUpdate(List<DevProjectPageSectionEntity> updatedSectionList) {

        UpdatePageSectionRequest request = new UpdatePageSectionRequest();
        request.setSections(updatedSectionList);
        AppProxy.get().updatePageSection(request, new AsyncCallback<RpcResult<UpdatePageSectionResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdatePageSectionResponse> result) {
                if (result.isSuccess()) {
                    fireEvent(CommonEvent.updateEvent(result.getData().getPage()));
                    loadPage(result.getData().getPage().getId(), "");
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });

    }

    public void loadCommit(DevProjectPageCommitEntity commit) {
        loadPage(pageEntity.getId(), commit.getId());
    }

    interface PageEditorUiBinder extends UiBinder<FlowPanel, PageEditor> {
    }

    private final CommonEventHandler itemHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent commonEvent) {
            if (commonEvent.isCreate()) {
                WikiBaseComponent source = (WikiBaseComponent) commonEvent.getSource();
                String kind = commonEvent.getValue();

                DevProjectPageSectionEntity section = new DevProjectPageSectionEntity();
                section.setSectionId(StringUtil.randomString(8));
                section.setKind(kind);
                section.setPageId(pageEntity.getId());
                section.setContent("");

                IWikiComponent component = WikiContext.get().createComponent(kind);
                component.initComponent(wikiPageContext, section);
                component.addCommonHandler(itemHandler); // Don't forget to attach the handler!
                sections.add(component);
                int sourceIndex = page.getWidgetIndex(source);
                int insertIndex = sourceIndex + 1;

                page.insert(component.getRootWidget(), insertIndex);

                component.focus();
            }
        }
    };
}