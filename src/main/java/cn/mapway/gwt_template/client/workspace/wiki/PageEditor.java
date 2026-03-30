package cn.mapway.gwt_template.client.workspace.wiki;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DevProjectPageEntity;
import cn.mapway.gwt_template.shared.db.DevProjectPageSectionEntity;
import cn.mapway.gwt_template.shared.doc.SectionKind;
import cn.mapway.gwt_template.shared.rpc.project.wiki.QueryPageSectionRequest;
import cn.mapway.gwt_template.shared.rpc.project.wiki.QueryPageSectionResponse;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import elemental2.dom.DomGlobal;

/**
 * WIKI 页面编辑器
 */
public class PageEditor extends CommonEventComposite implements IData<DevProjectPageEntity> {
    private static final PageEditorUiBinder ourUiBinder = GWT.create(PageEditorUiBinder.class);

    @UiField
    FlowPanel page;
    DevProjectPageEntity pageEntity;

    public PageEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
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
        QueryPageSectionRequest request = new QueryPageSectionRequest();
        request.setPageId(pageEntity.getId());
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
        for (DevProjectPageSectionEntity section : data.getSections()) {
            SectionKind kind = SectionKind.fromInt(section.getKind());
            switch (kind) {
                case PAGE:
                    PageHeaderItem item = new PageHeaderItem();
                    item.setPage(pageEntity);
                    item.setData(section);
                    page.add(item);
                    break;
                default:
                    EditableItem sectionItem = new EditableItem();
                    sectionItem.setData(section);
                    sectionItem.addCommonHandler(itemHandler);
                    page.add(sectionItem);
            }

        }
        DomGlobal.console.log("find sections ", data.getSections().size());
    }    private final CommonEventHandler itemHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent commonEvent) {
            if (commonEvent.isCreate()) {
                // 1. Identify the source and the requested kind
                EditableItem source = (EditableItem) commonEvent.getSource();
                SectionKind kind = commonEvent.getValue();
                DevProjectPageSectionEntity section = new DevProjectPageSectionEntity();
                section.setKind(kind.value);
                // 2. Create the new Data and UI component
                EditableItem newCreated = new EditableItem();
                newCreated.setData(section);
                newCreated.addCommonHandler(itemHandler); // Don't forget to attach the handler!

                // 3. Find the current position
                int sourceIndex = page.getWidgetIndex(source);
                int insertIndex = sourceIndex + 1;

                // 4. Update UI
                page.insert(newCreated, insertIndex);


                // 6. Optional: Set focus to the new item immediately
                newCreated.focus();
            }
        }
    };

    interface PageEditorUiBinder extends UiBinder<FlowPanel, PageEditor> {
    }




}