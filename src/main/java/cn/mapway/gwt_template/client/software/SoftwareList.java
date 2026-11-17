package cn.mapway.gwt_template.client.software;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.shared.db.SysSoftwareEntity;
import cn.mapway.gwt_template.shared.db.SysSoftwareFileEntity;
import cn.mapway.gwt_template.shared.rpc.soft.DeleteSoftwareFileRequest;
import cn.mapway.gwt_template.shared.rpc.soft.DeleteSoftwareFileResponse;
import cn.mapway.gwt_template.shared.rpc.soft.QuerySoftwareFilesRequest;
import cn.mapway.gwt_template.shared.rpc.soft.QuerySoftwareFilesResponse;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.Header;
import cn.mapway.ui.client.widget.buttons.DeleteButton;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import elemental2.promise.IThenable;

import java.util.*;

public class SoftwareList extends CommonEventComposite implements IData<SysSoftwareEntity> {
    private static final SoftwareListUiBinder ourUiBinder = GWT.create(SoftwareListUiBinder.class);
    private final ClickHandler confirmDelete = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            DeleteButton deleteButton = (DeleteButton) event.getSource();
            SysSoftwareFileEntity file = (SysSoftwareFileEntity) deleteButton.getData();
            String msg = "删除文件" + file.getName() + "?";
            ClientContext.get().confirmDelete(msg).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
                @Override
                public IThenable<Object> onInvoke(Void p0) {
                    doDelete(file);
                    return null;
                }
            });
        }
    };
    @UiField
    Label lbKey;
    @UiField
    Image icon;
    @UiField
    Label lbName;
    @UiField
    Label lbSummary;
    @UiField
    Label lbSet;
    @UiField
    Label lbCode;
    @UiField
    FlexTable list;
    @UiField
    SStyle style;
    @UiField FlowPanel details;
    @UiField FlowPanel filters;
    @UiField ListBox osFilter;
    @UiField ListBox archFilter;
    @UiField Label status;
    @UiField Button retry;
    private int sortColumn = 0;
    private boolean sortAscending = true;
    private int requestSequence;
    private List<SysSoftwareFileEntity> files = new ArrayList<>();
    private SysSoftwareEntity software;

    public SoftwareList() {
        initWidget(ourUiBinder.createAndBindUi(this));
        osFilter.getElement().setAttribute("aria-label", "筛选操作系统");
        archFilter.getElement().setAttribute("aria-label", "筛选架构");
        status.getElement().setAttribute("role", "status");
        osFilter.addChangeHandler(event -> renderFiles());
        archFilter.addChangeHandler(event -> renderFiles());
        retry.addClickHandler(event -> loadFiles());
        setData(null);
    }

    @Override
    public SysSoftwareEntity getData() {
        return software;
    }

    @Override
    public void setData(SysSoftwareEntity obj) {
        software = obj;
        requestSequence++;
        list.removeAllRows();
        files.clear();
        retry.setVisible(false);
        details.setVisible(obj != null);
        filters.setVisible(false);
        if (obj == null) {
            status.setText("请选择左侧软件查看可下载文件");
            return;
        }
        toUI();
    }

    private void toUI() {
        lbKey.setText(software.getToken());
        lbName.setText(software.getName());
        lbSummary.setText(software.getSummary());
        lbSet.setText("软件集: " + (software.getSoftwareSet() == null || software.getSoftwareSet().isEmpty() ? "未分组" : software.getSoftwareSet()));
        lbCode.setText("code: " + (software.getCode() == null || software.getCode().isEmpty() ? "-" : software.getCode()));
        icon.setUrl(software.getLogo());
        loadFiles();
    }

    private void loadFiles() {
        if (software == null) { return; }
        final int sequence = ++requestSequence;
        list.removeAllRows();
        files.clear();
        filters.setVisible(false);
        retry.setVisible(false);
        status.setText("正在加载文件…");
        QuerySoftwareFilesRequest request = new QuerySoftwareFilesRequest();
        request.setSoftwareId(software.getId());
        AppProxy.get().querySoftwareFiles(request, new AsyncAdaptor<RpcResult<QuerySoftwareFilesResponse>>() {
            @Override
            public void onData(RpcResult<QuerySoftwareFilesResponse> result) {
                if (sequence != requestSequence) { return; }
                files = result.getData().getFiles() == null ? new ArrayList<>() : new ArrayList<>(result.getData().getFiles());
                fillFilter(osFilter, true);
                fillFilter(archFilter, false);
                filters.setVisible(!files.isEmpty());
                renderFiles();
            }
            @Override
            public void onFailure(Throwable caught) {
                if (sequence != requestSequence) { return; }
                super.onFailure(caught);
                status.setText("文件加载失败，请重试");
                retry.setVisible(true);
            }

            @Override
            public boolean onError(RpcResult<QuerySoftwareFilesResponse> result) {
                if (sequence != requestSequence) { return true; }
                onFailure(new Exception(result.getMessage()));
                return true;
            }

        });
    }

    private void doDelete(SysSoftwareFileEntity file) {
        final String softwareId = software == null ? null : software.getId();
        DeleteSoftwareFileRequest request = new DeleteSoftwareFileRequest();
        request.setFileId(file.getId());
        AppProxy.get().deleteSoftwareFile(request, new AsyncAdaptor<RpcResult<DeleteSoftwareFileResponse>>() {
            @Override
            public void onData(RpcResult<DeleteSoftwareFileResponse> result) {
                if (software != null && software.getId().equals(softwareId)) {
                    loadFiles();
                }
            }
        });
    }

    private void renderFiles() {
        Map<String, List<SysSoftwareFileEntity>> versions = new HashMap<>();
        for (SysSoftwareFileEntity item : files) {
            if (!matches(osFilter, item.getOs()) || !matches(archFilter, item.getArch())) { continue; }
            List<SysSoftwareFileEntity> list = versions.get(item.getVersion());
            if (list == null) {
                list = new ArrayList<>();
                versions.put(item.getVersion(), list);
            }
            list.add(item);
        }
        list.removeAllRows();
        String[] headings = {"文件名称", "系统 / 架构", "文件大小", "校验值", "更新时间", "下载", "说明", "操作"};
        String[] widths = {"25%", "12%", "8%", "11%", "15%", "5%", "19%", "5%"};
        for (int i = 0; i < headings.length; i++) {
            if (i == 0 || i == 2 || i == 4) {
                final int column = i;
                boolean active = sortColumn == column;
                Button sort = new Button(headings[i] + (active ? (sortAscending ? " ↑" : " ↓") : " ↕"));
                sort.setStyleName(style.sortButton());
                sort.setTitle("按" + headings[i] + (active && sortAscending ? "降序" : "升序") + "排列");
                sort.getElement().setAttribute("aria-label", sort.getTitle());
                sort.addClickHandler(event -> {
                    sortAscending = sortColumn == column ? !sortAscending : true;
                    sortColumn = column;
                    renderFiles();
                    Widget heading = list.getWidget(0, column);
                    if (heading instanceof Button) { ((Button) heading).setFocus(true); }
                });
                list.setWidget(0, i, sort);
                list.getCellFormatter().getElement(0, i).setAttribute("aria-sort",
                        active ? (sortAscending ? "ascending" : "descending") : "none");
            } else {
                list.setText(0, i, headings[i]);
            }
            list.getCellFormatter().setWidth(0, i, widths[i]);
            list.getCellFormatter().getElement(0, i).setAttribute("role", "columnheader");
        }
        list.getRowFormatter().setStyleName(0, style.heading());
        status.setText(files.isEmpty() ? "暂无文件" : versions.isEmpty() ? "没有符合筛选条件的文件" : "");
        int row = 0;
        int col = 0;
        HTMLTable.RowFormatter rowFormatter = list.getRowFormatter();
        List<String> versionNames = new ArrayList<>(versions.keySet());
        Collections.sort(versionNames, Comparator.nullsLast(Comparator.reverseOrder()));
        if (versionNames.remove("latest")) { versionNames.add(0, "latest"); }
        for (String version : versionNames) {
            row++;
            col = 0;
            Label label = new Label(version);
            label.addStyleName(style.label());
            list.setWidget(row, col++, label);
            list.getFlexCellFormatter().setColSpan(row, 0, 8);
            List<SysSoftwareFileEntity> entityList = versions.get(version);
            Collections.sort(entityList, this::compareFiles);
            for (SysSoftwareFileEntity item : entityList) {
                row++;
                col = 0;
                list.setWidget(row, col++, new Header(item.getName()));
                list.setWidget(row, col++, new Label(item.getOs() + "/" + item.getArch()));
                list.setWidget(row, col++, new Label(StringUtil.formatFileSize(item.getSize())));
                Label hashLabel = new Label(shortHash(item.getHash()));
                if (item.getHash() != null && !item.getHash().isEmpty()) {
                    hashLabel.setTitle(item.getHash());
                }
                list.setWidget(row, col++, hashLabel);
                list.setWidget(row, col++, new Label(StringUtil.formatDate(item.getCreateTime())));
                Anchor anchor = new Anchor("下载");
                anchor.setHref(item.getLocation());
                anchor.setTarget("_blank");
                list.setWidget(row, col++, anchor);
                list.setWidget(row, col++, new Label(item.getSummary()));
                DeleteButton deleteButton = new DeleteButton();
                deleteButton.setData(item);
                deleteButton.addClickHandler(confirmDelete);
                list.setWidget(row, col++, deleteButton);
                rowFormatter.setStyleName(row, style.row());
            }
        }
    }

    private int compareFiles(SysSoftwareFileEntity first, SysSoftwareFileEntity second) {
        int comparison;
        if (sortColumn == 2) {
            if (first.getSize() == null || second.getSize() == null) {
                return compareMissing(first.getSize(), second.getSize());
            }
            comparison = first.getSize().compareTo(second.getSize());
        } else if (sortColumn == 4) {
            if (first.getCreateTime() == null || second.getCreateTime() == null) {
                return compareMissing(first.getCreateTime(), second.getCreateTime());
            }
            comparison = Long.compare(first.getCreateTime().getTime(), second.getCreateTime().getTime());
        } else {
            if (first.getName() == null || second.getName() == null) {
                return compareMissing(first.getName(), second.getName());
            }
            comparison = first.getName().compareToIgnoreCase(second.getName());
        }
        if (comparison != 0) { return sortAscending ? comparison : -comparison; }
        String firstName = first.getName() == null ? "" : first.getName();
        String secondName = second.getName() == null ? "" : second.getName();
        return firstName.compareToIgnoreCase(secondName);
    }

    // Missing values remain at the end in either direction.
    private int compareMissing(Object first, Object second) {
        return first == null ? (second == null ? 0 : 1) : -1;
    }

    private void fillFilter(ListBox box, boolean os) {
        String previous = box.getSelectedIndex() < 0 ? "" : box.getSelectedValue();
        Set<String> values = new TreeSet<>();
        for (SysSoftwareFileEntity file : files) {
            String value = os ? file.getOs() : file.getArch();
            if (value != null && !value.isEmpty()) { values.add(value); }
        }
        box.clear();
        box.addItem("全部", "");
        for (String value : values) {
            box.addItem(value, value);
            if (value.equals(previous)) { box.setSelectedIndex(box.getItemCount() - 1); }
        }
    }

    private boolean matches(ListBox box, String value) {
        return box.getSelectedValue().isEmpty() || box.getSelectedValue().equals(value);
    }

    private String shortHash(String hash) {
        if (hash == null || hash.isEmpty()) {
            return "-";
        }
        return hash.length() > 12 ? hash.substring(0, 12) : hash;
    }

    interface SStyle extends CssResource {

        String sortButton();
        String heading();
        String infoBar();
        String filterLabel();
        String filterSelect();
        String filters();
        String status();
        String summary();

        String name();

        String box();

        String list();

        String key();

        String row();

        String label();

        String lh();
    }

    interface SoftwareListUiBinder extends UiBinder<ScrollPanel, SoftwareList> {
    }
}