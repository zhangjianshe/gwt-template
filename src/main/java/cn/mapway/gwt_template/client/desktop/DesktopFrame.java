package cn.mapway.gwt_template.client.desktop;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DesktopItemEntity;
import cn.mapway.gwt_template.shared.rpc.desktop.DeleteDesktopRequest;
import cn.mapway.gwt_template.shared.rpc.desktop.DeleteDesktopResponse;
import cn.mapway.gwt_template.shared.rpc.desktop.QueryDesktopRequest;
import cn.mapway.gwt_template.shared.rpc.desktop.QueryDesktopResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.BaseAbstractModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import elemental2.promise.IThenable;
import org.jspecify.annotations.Nullable;

import static cn.mapway.gwt_template.client.desktop.DesktopFrame.MODULE_CODE;


@ModuleMarker(
        value = MODULE_CODE,
        name = "工作台",
        summary = "Personal Desktop",
        unicode = Fonts.CONSOLE
)
public class DesktopFrame extends BaseAbstractModule {
    public static final String MODULE_CODE = "desktop_frame";
    private static final DesktopFrameUiBinder ourUiBinder = GWT.create(DesktopFrameUiBinder.class);
    @UiField
    HTMLPanel panel;
    @UiField
    FlexTable table;
    @UiField
    Label btnAdd;
    @UiField
    MessageList messageList;
    @UiField
    TabLayoutPanel tab;
    @UiField
    HorizontalPanel toolsBar;
    @UiField
    AiButton btnBroadcast;
    @UiField
    LayoutPanel layout;

    public DesktopFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
        tab.addSelectionHandler(event -> {
            if (event.getSelectedItem() == 0) {
                messageList.loadMessages(false, 20, 1);
                layout.setWidgetVisible(toolsBar, false);

            } else {
                messageList.loadMessages(true, 20, 1);
                layout.setWidgetVisible(toolsBar, true);
            }
        });
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        boolean b = super.initialize(parentModule, parameter);
        load();
        tab.selectTab(0, true);
        return b;
    }

    private void confirmDelete(DesktopItemEntity value) {
        String message = "删除快捷按钮" + value.getName() + "?";
        ClientContext.get().confirmDelete(message).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
            @Override
            public @Nullable IThenable<Object> onInvoke(Void p0) {
                doDelete(value);
                return null;
            }
        });
    }

    private void doDelete(DesktopItemEntity value) {
        DeleteDesktopRequest request = new DeleteDesktopRequest();
        request.setItemId(value.getId());
        AppProxy.get().deleteDesktop(request, new AsyncCallback<RpcResult<DeleteDesktopResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<DeleteDesktopResponse> result) {
                if (result.isSuccess()) {
                    load();
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    private void load() {
        AppProxy.get().queryDesktop(new QueryDesktopRequest(), new AsyncCallback<RpcResult<QueryDesktopResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryDesktopResponse> result) {
                if (result.isSuccess()) {
                    renderItem(result.getData());
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });

    }

    private void renderItem(QueryDesktopResponse data) {
        int row = 0;
        int col = 0;
        int COL_COUNT = 5;
        table.removeAllRows();
        for (DesktopItemEntity entity : data.getItems()) {
            if (col > COL_COUNT) {
                row++;
                col = 0;
            }
            DesktopItem item = new DesktopItem();
            item.addCommonHandler(itemHandler);
            item.setData(entity);
            table.setWidget(row, col++, item);
        }
        if (col > COL_COUNT) {
            row++;
            col = 0;
        }
        table.setWidget(row, col++, btnAdd);
    }

    @UiHandler("btnAdd")
    public void btnAddClick(ClickEvent event) {
        edit(null);
    }

    @UiHandler("btnBroadcast")
    public void btnBroadcastClick(ClickEvent event) {
        Dialog<SendMessagePanel> dialog = SendMessagePanel.getDialog(true);
        dialog.addCommonHandler(event1 -> {
            if (event1.isOk()) {
                messageList.loadMessages(true, 20, 1);
            }
            dialog.hide();
        });
        dialog.center();
        dialog.getContent().setData("");
    }


    private void edit(DesktopItemEntity item) {
        Dialog<DesktopEditor> dialog = DesktopEditor.getDialog(true);
        dialog.addCommonHandler(event -> {
            if (event.isUpdate()) {
                load();
                dialog.hide();
            } else if (event.isClose()) {
                dialog.hide();
            }
        });
        dialog.getContent().setData(item);
        dialog.center();
    }

    interface DesktopFrameUiBinder extends UiBinder<DockLayoutPanel, DesktopFrame> {
    }

    private final CommonEventHandler itemHandler = event -> {
        if (event.isEdit()) {
            edit(event.getValue());
        } else if (event.isDelete()) {
            confirmDelete(event.getValue());
        } else if (event.isClick()) {
            DesktopItemEntity value = event.getValue();
            if (StringUtil.isNotBlank(value.getData())) {
                Window.open(value.getData(), "_blank", "");
            }
        }
    };


}