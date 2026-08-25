package cn.mapway.gwt_template.client.dns.traefik;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.widget.Head;
import cn.mapway.gwt_template.shared.db.AppServiceEntity;
import cn.mapway.gwt_template.shared.rpc.app.DeleteAppServiceRequest;
import cn.mapway.gwt_template.shared.rpc.app.DeleteAppServiceResponse;
import cn.mapway.gwt_template.shared.rpc.app.QueryAppServiceRequest;
import cn.mapway.gwt_template.shared.rpc.app.QueryAppServiceResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.BaseAbstractModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.Header;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.buttons.AiCheckBox;
import cn.mapway.ui.client.widget.buttons.DeleteButton;
import cn.mapway.ui.client.widget.buttons.EditButton;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.panel.MessagePanel;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import elemental2.promise.IThenable;
import org.jspecify.annotations.Nullable;

import java.util.List;

@ModuleMarker(
        value = TraefikFrame.MODULE_CODE,
        name = "服务配置",
        summary = "Traefik config",
        unicode = Fonts.APPS
)
public class TraefikFrame extends BaseAbstractModule {
    public final static String MODULE_CODE = "traefik_frame";
    private static final TraefikFrameUiBinder ourUiBinder = GWT.create(TraefikFrameUiBinder.class);
    @UiField
    AiButton btnAdd;
    @UiField
    FlexTable list;
    @UiField
    MessagePanel messagePanel;
    @UiField
    HorizontalPanel tools;
    @UiField
    AiButton btnTest;
    ClickHandler deleteHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            DeleteButton source = (DeleteButton) event.getSource();
            AppServiceEntity entity = (AppServiceEntity) source.getData();
            confirmDelete(entity);
        }
    };

    public TraefikFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    private void edit(AppServiceEntity entity) {
        Dialog<AppServiceEditor> dialog = AppServiceEditor.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isOk()) {
                    load();
                    dialog.hide();
                } else if (event.isClose()) {
                    dialog.hide();
                }
            }
        });
        dialog.center();
        dialog.getContent().setData(entity);
    }

    @Override
    public HorizontalPanel getTools() {
        return tools;
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        super.initialize(parentModule, parameter);
        load();
        return true;
    }

    private void load() {
        AppProxy.get().queryAppService(new QueryAppServiceRequest(), new AsyncCallback<RpcResult<QueryAppServiceResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryAppServiceResponse> result) {
                if (result.isSuccess()) {
                    renderRecords(result.getData());
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }    ClickHandler editHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            EditButton source = (EditButton) event.getSource();
            AppServiceEntity entity = (AppServiceEntity) source.getData();
            edit(entity);
        }
    };

    private void renderRecords(QueryAppServiceResponse data) {
        if (data.getServices().isEmpty()) {
            messagePanel.setVisible(true);
            messagePanel.setText("目前还没有服务");
            messagePanel.setHeight("150px");
            list.setVisible(false);
        } else {
            messagePanel.setVisible(false);
            list.setVisible(true);
            list.removeAllRows();
            int col = 0;
            int row = 0;

            list.setWidget(row, col++, new Head("#"));
            list.setWidget(row, col++, new Head("名称"));
            list.setWidget(row, col++, new Head("规则"));
            list.setWidget(row, col++, new Head("启用"));
            list.setWidget(row, col++, new Head("端点"));
            list.setWidget(row, col++, new Head("证书管理"));
            list.setWidget(row, col++, new Head("证书域名"));
            list.setWidget(row, col++, new Head("负载均衡"));
            list.setWidget(row, col++, new Head("操作"));

            for (AppServiceEntity entity : data.getServices()) {
                row++;
                col = 0;

                list.setWidget(row, col++, new Label("" + row));
                list.setWidget(row, col++, new Header(entity.getName()));
                Label ruel = new Label(entity.getRule());
                ruel.setStyleName("ai-code");
                list.setWidget(row, col++, ruel);

                AiCheckBox checkBox = new AiCheckBox();
                checkBox.setEnabled(false);
                checkBox.setValue(entity.getActive());
                list.setWidget(row, col++, checkBox);

                list.setText(row, col++, entity.getEndPoints());
                list.setText(row, col++, entity.getTls());
                list.setText(row, col++, entity.getDomains());

                VerticalPanel vp = new VerticalPanel();
                List<String> strings = StringUtil.splitIgnoreBlank(entity.getBalancer(), "\n");
                for (String item : strings) {
                    vp.add(new Label(item));
                }
                list.setWidget(row, col++, vp);

                HorizontalPanel hp1 = new HorizontalPanel();
                hp1.setSpacing(4);
                hp1.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
                EditButton editButton = new EditButton();
                editButton.setData(entity);
                editButton.addClickHandler(editHandler);
                hp1.add(editButton);

                DeleteButton deleteButton = new DeleteButton();
                deleteButton.setData(entity);
                deleteButton.addClickHandler(deleteHandler);
                hp1.add(deleteButton);
                list.setWidget(row, col++, hp1);
            }
            HTMLTable.ColumnFormatter columnFormatter = list.getColumnFormatter();
            columnFormatter.setWidth(0, "60");
            columnFormatter.setWidth(1, "120");
            columnFormatter.setWidth(8, "60");
        }

    }

    private void confirmDelete(AppServiceEntity entity) {
        String message = "删除应用" + entity.getName();
        ClientContext.get().confirmDelete(message).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
            @Override
            public @Nullable IThenable<Object> onInvoke(Void p0) {
                doDelete(entity);
                return null;
            }
        });
    }

    private void doDelete(AppServiceEntity entity) {
        DeleteAppServiceRequest request = new DeleteAppServiceRequest();
        request.setServiceId(entity.getId());
        AppProxy.get().deleteAppService(request, new AsyncCallback<RpcResult<DeleteAppServiceResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<DeleteAppServiceResponse> result) {
                load();
            }
        });
    }

    @UiHandler("btnAdd")
    public void btnAddClick(ClickEvent event) {
        edit(null);
    }

    @UiHandler("btnTest")
    public void btnTestClick(ClickEvent event) {
        String url = GWT.getHostPageBaseURL() + "../api/v1/traefik/index";
        Window.open(url, "traefik_test", "");
    }

    interface TraefikFrameUiBinder extends UiBinder<DockLayoutPanel, TraefikFrame> {
    }




}