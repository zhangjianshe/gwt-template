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
    FlexTable list;
    @UiField
    SStyle style;
    private SysSoftwareEntity software;

    public SoftwareList() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public SysSoftwareEntity getData() {
        return software;
    }

    @Override
    public void setData(SysSoftwareEntity obj) {
        software = obj;
        toUI();
    }

    private void toUI() {
        lbKey.setText(software.getToken());
        lbName.setText(software.getName());
        lbSummary.setText(software.getSummary());
        icon.setUrl(software.getLogo());
        loadFiles();
    }

    private void loadFiles() {
        QuerySoftwareFilesRequest request = new QuerySoftwareFilesRequest();
        request.setSoftwareId(software.getId());
        AppProxy.get().querySoftwareFiles(request, new AsyncAdaptor<RpcResult<QuerySoftwareFilesResponse>>() {
            @Override
            public void onData(RpcResult<QuerySoftwareFilesResponse> result) {
                renderFiles(result.getData());
            }
        });
    }

    private void doDelete(SysSoftwareFileEntity file) {
        DeleteSoftwareFileRequest request = new DeleteSoftwareFileRequest();
        request.setFileId(file.getId());
        AppProxy.get().deleteSoftwareFile(request, new AsyncAdaptor<RpcResult<DeleteSoftwareFileResponse>>() {
            @Override
            public void onData(RpcResult<DeleteSoftwareFileResponse> result) {
                loadFiles();
            }
        });
    }

    private void renderFiles(QuerySoftwareFilesResponse data) {
        Map<String, List<SysSoftwareFileEntity>> versions = new HashMap<>();
        for (SysSoftwareFileEntity item : data.getFiles()) {
            List<SysSoftwareFileEntity> list = versions.get(item.getVersion());
            if (list == null) {
                list = new ArrayList<>();
                versions.put(item.getVersion(), list);
            }
            list.add(item);
        }
        list.removeAllRows();
        int row = -1;
        int col = 0;
        HTMLTable.RowFormatter rowFormatter = list.getRowFormatter();
        for (String version : versions.keySet()) {
            row++;
            col = 0;
            Label label = new Label(version);
            label.addStyleName(style.label());
            list.setWidget(row, col++, label);
            List<SysSoftwareFileEntity> entityList = versions.get(version);
            Collections.sort(entityList, (o1, o2) -> {
                if (o1.getArch() == null || o2.getArch() == null) {
                    return 0;
                }
                return o1.getArch().compareTo(o2.getArch());
            });
            for (SysSoftwareFileEntity item : entityList) {
                row++;
                col = 0;
                list.setWidget(row, col++, new Header(item.getName()));
                list.setWidget(row, col++, new Label(item.getOs() + "/" + item.getArch()));
                list.setWidget(row, col++, new Label(StringUtil.formatFileSize(item.getSize())));
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

    interface SStyle extends CssResource {

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