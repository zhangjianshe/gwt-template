package cn.mapway.gwt_template.client.desktop;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.workspace.DevWorkspaceFrame;
import cn.mapway.gwt_template.client.workspace.calendar.ProjectCalendarWidget;
import cn.mapway.gwt_template.client.workspace.home.WorkspaceCard;
import cn.mapway.gwt_template.client.workspace.project.ProjectHomePanel;
import cn.mapway.gwt_template.shared.db.DesktopItemEntity;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.db.DevWorkspaceEntity;
import cn.mapway.gwt_template.shared.rpc.desktop.DeleteDesktopRequest;
import cn.mapway.gwt_template.shared.rpc.desktop.DeleteDesktopResponse;
import cn.mapway.gwt_template.shared.rpc.desktop.QueryDesktopRequest;
import cn.mapway.gwt_template.shared.rpc.desktop.QueryDesktopResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.*;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.AiLabel;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
public class DesktopFrame extends BaseAbstractModule implements RequiresResize {
    public static final String MODULE_CODE = "desktop_frame";
    private static final DesktopFrameUiBinder ourUiBinder = GWT.create(DesktopFrameUiBinder.class);
    @UiField
    HTMLPanel panel;
    @UiField
    DesktopItem btnAdd;
    @UiField
    TabLayoutPanel root;
    @UiField
    ScrollPanel content;
    @UiField
    HTMLPanel panelWorkspace;
    @UiField
    ProjectCalendarWidget calendar;
    @UiField
    DockLayoutPanel desktop;
    @UiField
    HTMLPanel panelProjects;
    Widget currentWidget = content;


    public DesktopFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
        btnAdd.addDomHandler(e -> {
            edit(null);
        }, ClickEvent.getType());
        btnAdd.setValue("img/plus.svg", "添加快捷方式");

        currentWidget = content;

        root.addSelectionHandler(event -> {
            Integer selectedItem = event.getSelectedItem();
            switch (selectedItem) {
                case 0:
                    break;
                case 1:
                    calendar.setData("9fe502023e8c443292138f6d0a7c7455");
                    break;
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
        panel.clear();
        for (DesktopItemEntity entity : data.getItems()) {
            DesktopItem item = new DesktopItem();
            item.addCommonHandler(itemHandler);
            item.setData(entity);
            panel.add(item);
        }
        panel.add(btnAdd);

        panelWorkspace.clear();
        for (DevWorkspaceEntity entity : data.getWorkspaces()) {
            WorkspaceCard card = new WorkspaceCard();
            card.setEnabledEdit(false);
            card.setData(entity);
            panelWorkspace.add(card);
            card.addCommonHandler(event -> {
                if (event.isSelect()) {
                    switchTo(event.getValue());
                }
            });
        }

        panelProjects.clear();
        for (DevProjectEntity project : data.getFavoriteProjects()) {
            AiLabel label = new AiLabel();
            label.setText(project.getName());
            label.setStyleName(AppResource.INSTANCE.styles().box());
            label.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    SwitchModuleData switchModuleData = new SwitchModuleData(ProjectHomePanel.MODULE_CODE, "");
                    switchModuleData.getParameters().put(project);
                    fireModuleEvent(DesktopFrame.this, CommonEvent.switchEvent(switchModuleData));
                }
            });
            panelProjects.add(label);
        }
    }

    private void switchTo(DevWorkspaceEntity entity) {
        SwitchModuleData switchModuleData = new SwitchModuleData(DevWorkspaceFrame.MODULE_CODE, "");
        switchModuleData.getParameters().put("workspace", entity);
        fireModuleEvent(this, CommonEvent.switchEvent(switchModuleData));
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

    @Override
    public void onResize() {
        root.onResize();
    }

    interface DesktopFrameUiBinder extends UiBinder<TabLayoutPanel, DesktopFrame> {
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