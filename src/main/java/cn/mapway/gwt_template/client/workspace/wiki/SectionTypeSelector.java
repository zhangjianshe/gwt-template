package cn.mapway.gwt_template.client.workspace.wiki;

import cn.mapway.gwt_template.client.workspace.wiki.component.WikiContext;
import cn.mapway.gwt_template.shared.wiki.WikiComponentManager;
import cn.mapway.gwt_template.shared.wiki.component.WikiComponentInformation;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;

import java.util.List;

public class SectionTypeSelector extends CommonEventComposite {
    private static final SectionTypeSelectorUiBinder ourUiBinder = GWT.create(SectionTypeSelectorUiBinder.class);
    private static Dialog<SectionTypeSelector> dialog;
    @UiField
    HTMLPanel content;
    @UiField
    SaveBar saveBar;

    public SectionTypeSelector() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Dialog<SectionTypeSelector> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<SectionTypeSelector> createOne() {
        SectionTypeSelector selector = new SectionTypeSelector();
        return new Dialog<>(selector, "选择组件");
    }

    public void load() {
        content.clear();
        WikiComponentManager manager = WikiContext.get();
        List<WikiComponentInformation> components = manager.getComponentsMetadata();
        for (WikiComponentInformation component : components) {
            WikiComponentItem wikiComponentItem = new WikiComponentItem();
            wikiComponentItem.setData(component);
            content.add(wikiComponentItem);
        }
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {

        } else {
            fireEvent(event);
        }
    }

    interface SectionTypeSelectorUiBinder extends UiBinder<DockLayoutPanel, SectionTypeSelector> {
    }
}