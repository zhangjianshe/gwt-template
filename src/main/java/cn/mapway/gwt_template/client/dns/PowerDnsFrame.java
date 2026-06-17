package cn.mapway.gwt_template.client.dns;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.shared.rpc.powerdns.*;
import cn.mapway.gwt_template.shared.rpc.powerdns.model.PowerDnsRRSet;
import cn.mapway.gwt_template.shared.rpc.powerdns.model.PowerDnsRecord;
import cn.mapway.gwt_template.shared.rpc.powerdns.model.PowerDnsZone;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.frame.ToolbarModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.mvc.attribute.DataCastor;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.Header;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.buttons.DeleteButton;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.tree.Tree;
import cn.mapway.ui.client.widget.tree.TreeItem;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import elemental2.promise.IThenable;
import org.jspecify.annotations.Nullable;


@ModuleMarker(
        value = PowerDnsFrame.MODULE_CODE,
        name = "PowerDns",
        summary = "PowerDNS Manager",
        unicode = Fonts.CMS
)
public class PowerDnsFrame extends ToolbarModule {
    public final static String MODULE_CODE = "power_dns_frame";

    interface PowerDnsFrameUiBinder extends UiBinder<DockLayoutPanel, PowerDnsFrame> {
    }

    private static final PowerDnsFrameUiBinder ourUiBinder = GWT.create(PowerDnsFrameUiBinder.class);
    @UiField
    Tree zoneTree;
    @UiField
    FlexTable records;

    public PowerDnsFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        super.initialize(parentModule, parameter);
        updateTools(tools);
        btnCreateRecord.setEnabled(false);
        loadZone();
        return true;
    }

    ClickHandler deleteZoneHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            event.stopPropagation();
            event.preventDefault();
            Widget widget = (Widget) event.getSource();
            IData<PowerDnsZone> data = (IData) widget;
            PowerDnsZone zone = data.getData();
            confirmDelete(zone);
        }
    };

    private void confirmDelete(PowerDnsZone zone) {
        String message = "删除ZONE" + zone.getName() + "?";
        ClientContext.get().confirmDelete(message).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
            @Override
            public @Nullable IThenable<Object> onInvoke(Void p0) {
                doDeleteZone(zone);
                return null;
            }
        });
    }

    private void doDeleteZone(PowerDnsZone zone) {
        DeleteZoneRequest request = new DeleteZoneRequest();
        request.setZoneId(zone.getName());
        AppProxy.get().deleteZone(request, new AsyncCallback<RpcResult<DeleteZoneResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<DeleteZoneResponse> result) {
                loadZone();
                records.removeAllRows();
            }
        });
    }

    private void loadZone() {
        AppProxy.get().queryZones(new QueryZonesRequest(), new AsyncCallback<RpcResult<QueryZonesResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryZonesResponse> result) {
                if (result.isSuccess()) {
                    zoneTree.clear();
                    for (PowerDnsZone zone : result.getData().getZones()) {
                        TreeItem treeItem = zoneTree.addItem(null, zone.getName());
                        DeleteButton deleteButton = new DeleteButton();

                        deleteButton.addClickHandler(deleteZoneHandler);
                        deleteButton.setData(zone);
                        treeItem.appendRightWidget(deleteButton);
                        treeItem.setData(zone);
                    }
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    @UiHandler("zoneTree")
    public void zoneTreeCommon(CommonEvent event) {
        if (event.isSelect()) {
            TreeItem item = event.getValue();
            PowerDnsZone zone = (PowerDnsZone) item.getData();
            loadZoneRecord(zone);
        }
    }

    @UiHandler("btnAddZone")
    public void btnAddZoneClick(ClickEvent event) {
        // Simple implementation: Use a Prompt dialog or custom DialogBox
        ClientContext.get().input("输入ZONE名称", "ZONE名称", "zone name", "", new Callback() {
            @Override
            public void onFailure(Object reason) {

            }

            @Override
            public void onSuccess(Object result) {
                String zoneName = DataCastor.castToString(result);
                if (StringUtil.isNotBlank(zoneName)) {
                    doAddZone(zoneName);
                }
            }
        });
    }

    @UiHandler("btnCreateRecord")
    public void btnCreateRecordClick(ClickEvent event) {
        editRecord(null,null);
    }

    private void doAddZone(String zoneName) {
        CreateZoneRequest request = new CreateZoneRequest();
        request.setZoneName(zoneName);
        request.setKind("Native"); // Standard default for most PowerDNS setups

        // 2. Execute RPC
        AppProxy.get().createZone(request, new AsyncCallback<RpcResult<CreateZoneResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, "Error adding zone: " + caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<CreateZoneResponse> result) {
                if (result.isSuccess()) {
                    ClientContext.get().toast(0, 0, "Zone added successfully!");
                    loadZone(); // Refresh the tree to show the new zone
                } else {
                    ClientContext.get().toast(0, 0, "Failed: " + result.getMessage());
                }
            }
        });
    }

    PowerDnsZone selectZoneId = null;

    private void loadZoneRecord(PowerDnsZone zone) {
        QueryRecordsRequest request = new QueryRecordsRequest();
        request.setZoneId(zone.getId());
        selectZoneId = zone;
        AppProxy.get().queryRecords(request, new AsyncCallback<RpcResult<QueryRecordsResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryRecordsResponse> result) {
                if (result.isSuccess()) {
                    renderList(result.getData());
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    private void renderList(QueryRecordsResponse response) {
        // Clear existing rows
        records.removeAllRows();
        btnCreateRecord.setEnabled(true);
        // Set Table Headers
        int col = 0;
        records.setWidget(0, col++, new Header("Name"));
        records.setWidget(0, col++, new Header("Type"));
        records.setWidget(0, col++, new Header("TTL"));
        records.setWidget(0, col++, new Header("Content"));
        records.setWidget(0, col++, new Header("操作"));

        records.getRowFormatter().addStyleName(0, style.headerStyle());

        int row = 1;
        if (response.getRrsets() != null) {
            for (PowerDnsRRSet rrset : response.getRrsets()) {
                // RRSets can contain multiple records of the same type (e.g., multiple A records)
                if (rrset.getRecords() != null) {
                    for (PowerDnsRecord record : rrset.getRecords()) {
                        records.setText(row, 0, rrset.getName());
                        records.setText(row, 1, rrset.getType());
                        records.setText(row, 2, String.valueOf(rrset.getTtl()));
                        records.setText(row, 3, record.getContent());

                        records.getRowFormatter().addStyleName(row, style.rowStyle());
                        records.getCellFormatter().addStyleName(row, 0, style.cellStyle());
                        records.getCellFormatter().addStyleName(row, 1, style.cellStyle());
                        records.getCellFormatter().addStyleName(row, 2, style.cellStyle());
                        records.getCellFormatter().addStyleName(row, 3, style.cellStyle());


                        // 2. 创建操作按钮容器
                        com.google.gwt.user.client.ui.HorizontalPanel hp = new com.google.gwt.user.client.ui.HorizontalPanel();
                        AiButton btnEdit = new AiButton("编辑");
                        AiButton btnDelete = new AiButton("删除");

                        // 绑定动作
                        btnEdit.addClickHandler(event -> editRecord(rrset, record));
                        btnDelete.addClickHandler(event -> deleteRecord(rrset, record));

                        hp.add(btnEdit);
                        hp.add(btnDelete);

                        // 将容器放入表格
                        records.setWidget(row, 4, hp);

                        row++;
                    }
                }

            }
        }
    }

    private void editRecord(PowerDnsRRSet rrset, PowerDnsRecord record) {

        Dialog<PowerDnsRecordEditor> dialog1 = PowerDnsRecordEditor.getDialog(true);
        dialog1.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isOk()) {
                    PowerDnsRRSet rrsetUpdated = event.getValue();
                    doSaveRecord(rrsetUpdated);
                } else {
                    dialog1.hide();
                }
            }
        });
        dialog1.getContent().setData(rrset);
        dialog1.center();
    }

    private void doSaveRecord(PowerDnsRRSet rrsetUpdated) {
        CreateOrUpdateRecordRequest request = new CreateOrUpdateRecordRequest();
        request.setRRSet(rrsetUpdated);
        AppProxy.get().createOrUpdateRecord(request, new AsyncAdaptor<RpcResult<CreateOrUpdateRecordResponse>>() {
            @Override
            public void onData(RpcResult<CreateOrUpdateRecordResponse> result) {

            }
        });
    }

    private void deleteRecord(PowerDnsRRSet rrset, PowerDnsRecord record) {
        // 1. 弹出确认框
        ClientContext.get().confirm("确定要删除记录 " + rrset.getName() + " 吗?").then(result -> {
            // 2. 调用删除逻辑
            DeleteRecordRequest request = new DeleteRecordRequest();
            request.setName(rrset.getName());
            request.setZoneId(selectZoneId.getId());
            // 设置必要的参数 (zoneId, name, type 等)
            AppProxy.get().deleteRecord(request, new AsyncCallback<RpcResult<DeleteRecordResponse>>() {
                @Override
                public void onFailure(Throwable caught) {
                    ClientContext.get().toast(0, 0, "删除失败");
                }

                @Override
                public void onSuccess(RpcResult<DeleteRecordResponse> result) {
                    // 刷新列表
                    loadZoneRecord(selectZoneId);
                }
            });
            return null;
        });
    }

    @UiField
    SStyle style;
    @UiField
    Button btnAddZone;
    @UiField
    AiButton btnCreateRecord;
    @UiField
    HorizontalPanel tools;

    interface SStyle extends CssResource {
        String box();

        String headerStyle();

        String centerPanel();

        String rowStyle();

        String cellStyle();

        String westPanel();

        String recordTable();
    }
}