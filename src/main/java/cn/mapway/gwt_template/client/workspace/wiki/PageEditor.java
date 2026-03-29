package cn.mapway.gwt_template.client.workspace.wiki;

import cn.mapway.gwt_template.shared.doc.PageDocument;
import cn.mapway.gwt_template.shared.doc.PageSection;
import cn.mapway.gwt_template.shared.doc.SectionKind;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import jsinterop.base.Js;
import jsinterop.base.JsArrayLike;

/**
 * WIKI 页面编辑器
 */
public class PageEditor extends CommonEventComposite implements IData<PageDocument> {
    private static final PageEditorUiBinder ourUiBinder = GWT.create(PageEditorUiBinder.class);

    @UiField
    FlowPanel page;
    PageDocument document;
    private final CommonEventHandler itemHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent commonEvent) {
            if (commonEvent.isCreate()) {
                // 1. Identify the source and the requested kind
                EditableItem source = (EditableItem) commonEvent.getSource();
                SectionKind kind = commonEvent.getValue();

                // 2. Create the new Data and UI component
                PageSection newSection = PageSection.create(kind.value);
                EditableItem newCreated = new EditableItem();
                newCreated.setData(newSection);
                newCreated.addCommonHandler(itemHandler); // Don't forget to attach the handler!

                // 3. Find the current position
                int sourceIndex = page.getWidgetIndex(source);
                int insertIndex = sourceIndex + 1;

                // 4. Update UI
                page.insert(newCreated, insertIndex);

                // 5. Update the Data Model (PageDocument)
                // Assuming document.sections is a JsArray or similar structure
                insertAt(document.sections, insertIndex, newSection);

                // 6. Optional: Set focus to the new item immediately
                newCreated.focus();
            }
        }
    };
    public PageEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    private native void insertAt(JsArrayLike array, int index, PageSection section) /*-{
        array.splice(index, 0, section);
    }-*/;

    @Override
    public PageDocument getData() {
        return document;
    }

    @Override
    public void setData(PageDocument pageDocument) {
        page.clear();
        document = pageDocument;
        if (document == null) {
            document = PageDocument.create();
            PageSection section = PageSection.create(SectionKind.H2.value);
            section.content = "章节1";
            document.sections.push(section);
            section = PageSection.create(SectionKind.H2.value);
            section.content = "章节2";
            document.sections.push(section);
        }
        toUI();
    }

    private void toUI() {
        for (int i = 0; i < document.sections.length; i++) {
            PageSection section = Js.uncheckedCast(document.sections.getAt(i));
            EditableItem item = new EditableItem();
            item.setData(section);
            item.addCommonHandler(itemHandler);
            page.add(item);
        }
    }

    interface PageEditorUiBinder extends UiBinder<FlowPanel, PageEditor> {
    }


}