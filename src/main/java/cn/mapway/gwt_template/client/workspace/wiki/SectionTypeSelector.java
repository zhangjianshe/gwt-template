package cn.mapway.gwt_template.client.workspace.wiki;

import cn.mapway.gwt_template.client.workspace.wiki.component.WikiContext;
import cn.mapway.gwt_template.shared.wiki.WikiComponentManager;
import cn.mapway.gwt_template.shared.wiki.component.WikiComponentInformation;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

public class SectionTypeSelector extends CommonEventComposite {
    private static final SectionTypeSelectorUiBinder ourUiBinder = GWT.create(SectionTypeSelectorUiBinder.class);
    private static Popup<SectionTypeSelector> dialog;
    private final ClickHandler itemSelected = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            WikiComponentItem source = (WikiComponentItem) event.getSource();
            WikiComponentInformation information = source.getData();
            fireEvent(CommonEvent.selectEvent(information));
        }
    };
    @UiField
    HTMLPanel content;
    @UiField
    SaveBar saveBar;
    @UiField
    ScrollPanel scroller;

    WikiComponentItem selectedItem = null;

    public SectionTypeSelector() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Popup<SectionTypeSelector> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Popup<SectionTypeSelector> createOne() {
        SectionTypeSelector selector = new SectionTypeSelector();
        return new Popup<>(selector);
    }

    public void load(String filter) {
        content.clear();
        selectedItem = null;
        WikiComponentManager manager = WikiContext.get();
        List<WikiComponentInformation> components = manager.getComponentsMetadata();

        String query = (filter == null) ? "" : filter.toLowerCase();

        for (WikiComponentInformation component : components) {
            // 过滤逻辑：匹配名称、别名或目录
            boolean matches = query.isEmpty()
                    || component.getName().toLowerCase().contains(query)
                    || (component.getAlias() != null && component.getAlias().toLowerCase().contains(query));

            if (matches && Boolean.TRUE.equals(component.getSelect())) {
                WikiComponentItem item = new WikiComponentItem();
                item.setData(component);
                item.addDomHandler(itemSelected, ClickEvent.getType());
                content.add(item);
            }
        }

        // 如果过滤后没有结果，可以考虑隐藏弹窗或显示“无匹配”
        if (content.getWidgetCount() == 0) {
            // dialog.hide();
        }
        selectFirst();
    }

    private void selectFirst() {
        for (int index = 0; index < content.getWidgetCount(); index++) {
            Widget widget = content.getWidget(index);
            if (widget instanceof WikiComponentItem) {
                WikiComponentItem item = (WikiComponentItem) widget;
                selectItem(item);
                break;
            }
        }
    }

    private void selectItem(WikiComponentItem item) {
        if (selectedItem != null) {
            selectedItem.setSelect(false);
        }
        selectedItem = item;
        if (selectedItem != null) {
            selectedItem.setSelect(true);
        }
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        scroller.getElement().focus();
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {

        } else {
            fireEvent(event);
        }
    }

    public void handleNavigation(int keyCode) {
        int count = content.getWidgetCount();
        if (count == 0) return;
        if (selectedItem == null) {
            selectFirst();
            return;
        }
        int widgetIndex = content.getWidgetIndex(selectedItem);
        if (keyCode == KeyCodes.KEY_DOWN) {
            int next = (widgetIndex + 1) % count;
            Widget widget = content.getWidget(next);
            if (widget instanceof WikiComponentItem) {
                selectItem((WikiComponentItem) widget);
            }
        } else if (keyCode == KeyCodes.KEY_UP) {
            int prev = (widgetIndex - 1 + count) % count;
            Widget widget = content.getWidget(prev);
            if (widget instanceof WikiComponentItem) {
                selectItem((WikiComponentItem) widget);
            }
        }
    }

    public void confirmSelected() {
        if (selectedItem != null) {
            fireEvent(CommonEvent.selectEvent(selectedItem.getData()));
        }
    }

    interface SectionTypeSelectorUiBinder extends UiBinder<DockLayoutPanel, SectionTypeSelector> {
    }
}