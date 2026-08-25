package cn.mapway.gwt_template.client.dashboard;

import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.ui.client.mvc.BaseAbstractModule;
import cn.mapway.ui.client.mvc.ModuleInfo;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;

public class WidgetExplorer extends CommonEventComposite {
    private static final WidgetExplorerUiBinder ourUiBinder = GWT.create(WidgetExplorerUiBinder.class);
    private static Dialog<WidgetExplorer> dialog;
    @UiField
    SaveBar saveBar;
    @UiField
    HTMLPanel list;
    WidgetItem selected = null;
    private final ClickHandler itemHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            WidgetItem source = (WidgetItem) event.getSource();
            ModuleInfo moduleInfo = source.getData();
            if (selected != null) {
                selected.setSelect(false);
            }
            selected = source;
            selected.setSelect(true);
            saveBar.setEnableSave(true);
            saveBar.msg(moduleInfo.name);
        }
    };

    public WidgetExplorer() {
        initWidget(ourUiBinder.createAndBindUi(this));
        saveBar.setEnableSave(false);
    }

    public static Dialog<WidgetExplorer> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<WidgetExplorer> createOne() {
        return new Dialog<>(new WidgetExplorer(), "选择组件");
    }


    public void load() {
        list.clear();
        for (ModuleInfo moduleInfo : BaseAbstractModule.getModuleFactory().getModules()) {
            if (moduleInfo.hasTag(AppConstant.TAG_WIDGET)) {
                WidgetItem item = new WidgetItem();
                item.setData(moduleInfo);
                item.addDomHandler(itemHandler, ClickEvent.getType());
                list.add(item);
            }
        }
        saveBar.setEnableSave(false);
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            fireEvent(CommonEvent.selectEvent(selected.getData()));
        } else {
            fireEvent(event);
        }
    }

    interface WidgetExplorerUiBinder extends UiBinder<DockLayoutPanel, WidgetExplorer> {
    }
}