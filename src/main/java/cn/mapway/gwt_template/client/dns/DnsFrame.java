package cn.mapway.gwt_template.client.dns;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.SysConfigEntity;
import cn.mapway.gwt_template.shared.rpc.config.QueryConfigListRequest;
import cn.mapway.gwt_template.shared.rpc.config.QueryConfigListResponse;
import cn.mapway.gwt_template.shared.rpc.dns.QueryDnsRequest;
import cn.mapway.gwt_template.shared.rpc.dns.QueryDnsResponse;
import cn.mapway.gwt_template.shared.rpc.dns.model.CloudflareConfig;
import cn.mapway.gwt_template.shared.rpc.dns.model.DnsEntry;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.frame.ToolbarModules;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.SearchBox;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.buttons.AiCheckBox;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.list.ListItem;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import elemental2.core.Global;
import jsinterop.base.Js;

import java.util.ArrayList;
import java.util.List;

import static cn.mapway.gwt_template.client.dns.DnsFrame.MODULE_CODE;

/**
 * DNS配置窗口
 */
@ModuleMarker(
        value = MODULE_CODE,
        name = "DNS配置",
        summary = "config the dns",
        unicode = Fonts.CMS
)
public class DnsFrame extends ToolbarModules {
    public static final String MODULE_CODE = "dns_frame";
    private static final DnsFrameUiBinder ourUiBinder = GWT.create(DnsFrameUiBinder.class);
    @UiField
    HorizontalPanel tools;
    @UiField
    FlexTable table;
    @UiField
    cn.mapway.ui.client.widget.list.List zoneList;
    @UiField
    SearchBox searchBox;
    @UiField
    Button btnUpdateIp;
    AiCheckBox selectAll;
    List<AiCheckBox> allItems = new ArrayList<AiCheckBox>();
    CloudflareConfig currentZone;

    public DnsFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
        selectAll = new AiCheckBox();
        selectAll.addValueChangeHandler(event -> {
            HTMLTable.RowFormatter rowFormatter = table.getRowFormatter();
            for (int row = 1; row < table.getRowCount(); row++) {
                if (rowFormatter.isVisible(row)) {
                    allItems.get(row - 1).setValue(event.getValue());
                } else {
                    allItems.get(row - 1).setValue(false);
                }
            }
            updateButton();
        });
    }

    private void updateButton() {
        btnUpdateIp.setEnabled(false);
        for (AiCheckBox checkBox : allItems) {
            if (checkBox.getValue()) {
                btnUpdateIp.setEnabled(true);
                break;
            }
        }

    }

    @Override
    protected void initializeSubsystem() {

    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        super.initialize(parentModule, parameter);
        loadData();
        updateTools(tools);
        return true;
    }

    private void loadData() {
        QueryConfigListRequest request = new QueryConfigListRequest();
        List<String> keys = new ArrayList<String>();
        keys.add(AppConstant.KEY_CLOUDFLARE_TOKEN);
        request.setKeys(keys);
        AppProxy.get().queryConfigList(request, new AsyncCallback<RpcResult<QueryConfigListResponse>>() {
            @Override
            public void onFailure(Throwable throwable) {

            }

            @Override
            public void onSuccess(RpcResult<QueryConfigListResponse> result) {
                if (result.isSuccess() && !result.getData().getConfigs().isEmpty()) {
                    renderData(result.getData().getConfigs().get(0));
                }
            }

        });
    }

    private void renderData(SysConfigEntity sysConfigEntity) {

        CloudflareConfig[] configJsArray = Js.uncheckedCast(Global.JSON.parse(sysConfigEntity.getValue()));
        zoneList.clear();
        for (int i = 0; i < configJsArray.length; i++) {
            CloudflareConfig config = configJsArray[i];
            ListItem listItem = new ListItem();
            listItem.setText(config.name);
            listItem.setIcon("");
            listItem.setData(config);
            zoneList.addItem(listItem);
        }

    }

    @UiHandler("zoneList")
    public void zoneListCommon(CommonEvent event) {
        if (event.isSelect()) {
            ListItem item = event.getValue();
            CloudflareConfig config = Js.uncheckedCast(item.getData());
            fetchDnsList(config);
        }
    }

    @UiHandler("searchBox")
    public void searchBoxCommon(ValueChangeEvent<String> event) {
        String filter = event.getValue();
        filterData(filter);
    }

    @UiHandler("btnUpdateIp")
    public void btnUpdateIpClick(ClickEvent event) {
        Dialog<DnsBatchEditor> dialog = DnsBatchEditor.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isOk()) {
                    // reload
                    fetchDnsList(currentZone);
                }
                dialog.hide();
            }
        });
        dialog.getContent().setZoneId(currentZone.zoneId);
        dialog.getContent().setData(getSelectList());
        dialog.center();
    }

    private List<DnsEntry> getSelectList() {

        List<DnsEntry> list = new ArrayList<>();
        for (AiCheckBox checkBox : allItems) {
            if (checkBox.getValue()) {
                list.add((DnsEntry) checkBox.getData());
            }
        }
        return list;
    }    ClickHandler editHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            AiButton button = (AiButton) event.getSource();
            DnsEntry dnsEntry = (DnsEntry) button.getData();
            editDns(dnsEntry);
        }
    };

    /**
     * 根据名称过滤
     *
     * @param filter
     */
    private void filterData(String filter) {
        HTMLTable.RowFormatter rowFormatter = table.getRowFormatter();
        boolean initShow = StringUtil.isBlank(filter);
        for (int row = 1; row < table.getRowCount(); row++) {
            DnsEntry entry = (DnsEntry) allItems.get(row - 1).getData();
            rowFormatter.setVisible(row, initShow || entry.getName().contains(filter) || entry.getContent().contains(filter));
        }
    }

    private void fetchDnsList(CloudflareConfig config) {
        currentZone = config;
        QueryDnsRequest request = new QueryDnsRequest();
        request.setZonId(config.zoneId);
        AppProxy.get().queryDns(request, new AsyncCallback<RpcResult<QueryDnsResponse>>() {
            @Override
            public void onFailure(Throwable throwable) {

            }

            @Override
            public void onSuccess(RpcResult<QueryDnsResponse> result) {
                if (result.isSuccess()) {
                    rendDnsList(result.getData().getDnsList());
                }
            }
        });
    }

    private void editDns(DnsEntry dnsEntry) {
        final Dialog<DnsEditor> dialog = DnsEditor.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isOk()) {
                    DnsEntry entry = event.getValue();
                    int index = indexOfEntry(entry.getId());
                    if (index > 0) {
                        updateTableRow(index, entry);
                    }
                } else if (event.isClose()) {
                    dialog.hide();
                }
            }
        });
        dialog.getContent().setZoneId(currentZone.zoneId);
        dialog.getContent().setData(dnsEntry);
        dialog.center();
    }

    private void updateTableRow(int index, DnsEntry entry) {
        int row = index;
        int col = 0;
        AiCheckBox checkBox = new AiCheckBox();
        checkBox.setData(entry);
        checkBox.addValueChangeHandler(event -> {
            updateButton();
        });
        allItems.add(checkBox);
        table.setWidget(row, col++, checkBox);
        table.setWidget(row, col++, new Label(entry.getName()));
        table.setWidget(row, col++, new Label(entry.getType()));
        table.setWidget(row, col++, new Label(entry.getContent()));
        table.setWidget(row, col++, new Label(String.valueOf(entry.getTtl())));
        table.setWidget(row, col++, new Label(entry.getComment()));
        AiButton btnEdit = new AiButton("编辑");
        btnEdit.setData(entry);

        btnEdit.addClickHandler(editHandler);
        table.setWidget(row, col++, btnEdit);
    }

    private int indexOfEntry(String id) {
        for (int i = 0; i < allItems.size(); i++) {
            DnsEntry entry = (DnsEntry) allItems.get(i).getData();
            if (entry.getId().equals(id)) {
                return i + 1;
            }
        }
        return -1;
    }

    private void rendDnsList(List<DnsEntry> dnsList) {
        table.removeAllRows();
        allItems.clear();
        updateButton();
        int row = 0;
        int col = 0;

        selectAll.setValue(false, false);
        table.setWidget(row, col++, selectAll);
        table.setWidget(row, col++, new Label("DNS NAME"));
        table.setWidget(row, col++, new Label("Type"));
        table.setWidget(row, col++, new Label("Content"));
        table.setWidget(row, col++, new Label("TTL"));
        table.setWidget(row, col++, new Label("注释"));
        table.setWidget(row, col++, new Label(""));

        for (DnsEntry entry : dnsList) {
            row++;
            col = 0;
            updateTableRow(row, entry);
        }

        HTMLTable.ColumnFormatter columnFormatter = table.getColumnFormatter();
        columnFormatter.setWidth(0, "80px");
        columnFormatter.setWidth(1, "200px");
        columnFormatter.setWidth(2, "60px");
        columnFormatter.setWidth(3, "200px");
        columnFormatter.setWidth(4, "80px");
        columnFormatter.setWidth(6, "90px");
        columnFormatter.setStyleName(6, "ai-right");
    }

    interface DnsFrameUiBinder extends UiBinder<DockLayoutPanel, DnsFrame> {
    }




}