package cn.mapway.gwt_template.client.log;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.SysLogEntity;
import cn.mapway.gwt_template.shared.rpc.log.LogLevel;
import cn.mapway.gwt_template.shared.rpc.log.QueryLogsRequest;
import cn.mapway.gwt_template.shared.rpc.log.QueryLogsResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.frame.SubsystemModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.Header;
import cn.mapway.ui.client.widget.SearchBox;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;

@ModuleMarker(
        name = "日志管理",
        value = LogFrame.MODULE_CODE,
        unicode = Fonts.LOGFILE,
        summary = "系统日志管理",
        order = 100
)
public class LogFrame extends SubsystemModule {
    public static final String MODULE_CODE = "log_frame";
    private static final LogFrameUiBinder ourUiBinder = GWT.create(LogFrameUiBinder.class);
    @UiField
    FlexTable logTable;
    @UiField
    AiButton btnNext;
    @UiField
    AiButton btnPrev;
    @UiField
    SearchBox searchBox;
    @UiField
    Label lbTotal;
    Integer page = 1;
    Integer pageSize = 50;

    public LogFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
        searchBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                page = 1;
                pageSize = 50;
                load();
            }
        });
    }

    @UiHandler("btnPrev")
    public void btnPrevClick(ClickEvent event) {
        page--;
        load();
    }

    @UiHandler("btnNext")
    public void btnNextClick(ClickEvent event) {
        page++;
        load();
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        boolean b = super.initialize(parentModule, parameter);
        page = 1;
        pageSize = 50;
        load();
        return true;
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @Override
    protected void initializeSubsystem() {

    }

    private void load() {
        QueryLogsRequest request = new QueryLogsRequest();
        request.setActionName(searchBox.getValue());
        request.setPage(page);
        request.setPageSize(pageSize);
        AppProxy.get().queryLogs(request, new AsyncCallback<RpcResult<QueryLogsResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryLogsResponse> result) {
                if (result.isSuccess()) {
                    renderLogs(result);

                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    private void renderLogs(RpcResult<QueryLogsResponse> result) {
        QueryLogsResponse data = result.getData();
        page = data.getPage();
        pageSize = data.getPageSize();
        int totalPage = ((int) data.getTotal() / pageSize) + (data.getTotal() % pageSize == 0 ? 0 : 1);
        lbTotal.setText(page + "/" + totalPage + " [" + data.getTotal() + "]");
        btnPrev.setEnabled(page > 1);
        btnNext.setEnabled(page < totalPage);

        logTable.removeAllRows();
        int col = 0;
        int row = 0;
        logTable.setWidget(row, col++, new Header("级别"));
        logTable.setWidget(row, col++, new Header("时间"));
        logTable.setWidget(row, col++, new Header("用户"));
        logTable.setWidget(row, col++, new Header("活动名称"));
        logTable.setWidget(row, col++, new Header("内容"));
        for (SysLogEntity log : data.getLogs()) {
            row++;
            col = 0;
            logTable.setText(row, col++, LogLevel.fromLevel(log.getLevel()).getName());
            logTable.setText(row, col++, StringUtil.formatDate(log.getCreateTime()));
            logTable.setText(row, col++, log.getUserName());
            logTable.setText(row, col++, log.getAction());
            logTable.setText(row, col++, log.getContent());
        }
    }

    interface LogFrameUiBinder extends UiBinder<DockLayoutPanel, LogFrame> {
    }
}