package cn.mapway.gwt_template.client.software;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.shared.db.SysSoftwareEntity;
import cn.mapway.gwt_template.shared.db.SysSoftwareFileEntity;
import cn.mapway.gwt_template.shared.rpc.soft.QuerySoftwareFilesRequest;
import cn.mapway.gwt_template.shared.rpc.soft.QuerySoftwareFilesResponse;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.Header;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoftwareList extends CommonEventComposite implements IData<SysSoftwareEntity> {
    private static final SoftwareListUiBinder ourUiBinder = GWT.create(SoftwareListUiBinder.class);
    @UiField
    Label lbKey;
    @UiField
    Image icon;
    @UiField
    Label lbName;
    @UiField
    Label lbSummary;
    @UiField
    HTMLPanel list;
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
        list.clear();
        for (String version : versions.keySet()) {
            Label label = new Label(version);
            label.addStyleName(style.label());
            list.add(label);
            HTMLPanel panel = new HTMLPanel("");
            panel.addStyleName(style.grid());
            for (SysSoftwareFileEntity item : versions.get(version)) {
                panel.add(new Header(item.getName()));
                panel.add(new Label(item.getOs() + "/" + item.getArch()));
                panel.add(new Label(StringUtil.formatFileSize(item.getSize())));
                panel.add(new Label(StringUtil.formatDate(item.getCreateTime())));
                Anchor anchor = new Anchor("下载");
                anchor.setHref("/upload/" + item.getLocation());
                anchor.setTarget("_blank");
                panel.add(anchor);
                panel.add(new Label(item.getSummary()));
            }
            list.add(panel);
        }
    }

    interface SStyle extends CssResource {

        String summary();

        String name();

        String box();

        String list();

        String key();

        String grid();

        String label();
    }

    interface SoftwareListUiBinder extends UiBinder<ScrollPanel, SoftwareList> {
    }
}