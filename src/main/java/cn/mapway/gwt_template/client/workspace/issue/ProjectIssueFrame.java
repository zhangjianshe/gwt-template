package cn.mapway.gwt_template.client.workspace.issue;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.client.workspace.widget.TaskPriorityDropdown;
import cn.mapway.gwt_template.shared.db.DevProjectIssueEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectIssueRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectIssueResponse;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectIssueRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectIssueResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskPriority;
import cn.mapway.gwt_template.shared.rpc.project.module.IssueState;
import cn.mapway.ui.client.mvc.attribute.DataCastor;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.buttons.AiCheckBox;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

public class ProjectIssueFrame extends CommonEventComposite implements IData<String> {
    private static final ProjectIssueFrameUiBinder ourUiBinder = GWT.create(ProjectIssueFrameUiBinder.class);
    private final int pageSize = 20;
    @UiField
    FlexTable table;
    @UiField
    SStyle style;
    @UiField
    AiButton btnCreate;
    @UiField
    AiCheckBox chkMyIssue;
    @UiField
    Label lblPageInfo;
    @UiField
    Label lblCurrentPage;
    @UiField
    AiButton btnPrev;
    @UiField
    AiButton btnNext;
    @UiField
    TaskPriorityDropdown ddlPriority;
    @UiField
    IssueStateDropdown ddlState;
    private int currentPage = 1;
    private String projectId;
    // 定义一个变量记录当前选中行
    private int selectedRowIndex = -1;

    public ProjectIssueFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
        initFilters();
        bindEvents();
    }

    private void initFilters() {
        ddlState.init(true);
    }

    @Override
    public String getData() {
        return projectId;
    }

    @Override
    public void setData(String projectId) {
        this.projectId = projectId;
        toUI();
    }

    private void toUI() {

        table.clear(); // Good practice before rebuilding
        reloadAll(1);
    }

    private void bindEvents() {
        table.addClickHandler(event -> {
            // 获取点击的单元格
            HTMLTable.Cell cell = table.getCellForEvent(event);
            if (cell != null) {
                int rowIndex = cell.getRowIndex();

                // 排除表头（第0行）
                if (rowIndex > 0) {
                    selectRow(rowIndex);
                }
            }
        });

        // 过滤条件变化触发查询
        ddlState.addValueChangeHandler(event -> {
            reloadAll(1);
        });
        ddlPriority.addValueChangeHandler(event -> reloadAll(1));
        chkMyIssue.addValueChangeHandler(event -> reloadAll(1));

        // 分页按钮
        btnPrev.addClickHandler(event -> {
            if (currentPage > 1) reloadAll(currentPage - 1);
        });
        btnNext.addClickHandler(event -> reloadAll(currentPage + 1));
    }

    private void selectRow(int rowIndex) {
        // 移除旧的选中样式
        if (selectedRowIndex != -1) {
            table.getRowFormatter().removeStyleName(selectedRowIndex, style.selectedRow());
        }

        // 添加新的选中样式
        selectedRowIndex = rowIndex;
        table.getRowFormatter().addStyleName(selectedRowIndex, style.selectedRow());

        // 这里可以触发右侧详情页的刷新
        // String issueId = table.getText(rowIndex, 0);
        // fireDetailEvent(issueId);
    }

    @UiHandler("btnCreate")
    public void btnCreateClick(ClickEvent event) {
        ClientContext.get().input("创建Issue", "输入问题标题", "不超过255个字符",
                "", new Callback() {
                    @Override
                    public void onFailure(Object reason) {

                    }

                    @Override
                    public void onSuccess(Object result) {
                        String title = DataCastor.castToString(result);
                        if (StringUtil.isNotBlank(title)) {
                            doCreate(title);
                        }
                    }
                });
    }

    private void doCreate(String title) {
        UpdateProjectIssueRequest request = new UpdateProjectIssueRequest();
        DevProjectIssueEntity entity = new DevProjectIssueEntity();

        entity.setProjectId(projectId); // 必须设置，否则后端无法通过成员检查
        entity.setName(title);
        request.setIssue(entity);

        AppProxy.get().updateProjectIssue(request, new AsyncAdaptor<RpcResult<UpdateProjectIssueResponse>>() {
            @Override
            public void onData(RpcResult<UpdateProjectIssueResponse> result) {
                if (result.isSuccess()) {
                    // 创建成功后重新加载
                    reloadAll(1);
                } else {
                    // 可以弹窗提示错误原因
                    GWT.log("创建失败: " + result.getMessage());
                }
            }
        });
    }

    private void reloadAll(int page) {
        this.currentPage = page;
        QueryProjectIssueRequest request = new QueryProjectIssueRequest();
        request.setProjectId(projectId);
        request.setPage(currentPage);
        request.setPageSize(pageSize);

        // 设置过滤条件
        request.setState(DataCastor.castToInteger(ddlState.getValue()));
        request.setPriority(DataCastor.castToInteger(ddlPriority.getValue()));

        if (chkMyIssue.getValue()) {
            request.setChargeId(Long.parseLong(ClientContext.get().getUserInfo().getId()));
        }

        AppProxy.get().queryProjectIssue(request, new AsyncAdaptor<RpcResult<QueryProjectIssueResponse>>() {
            @Override
            public void onData(RpcResult<QueryProjectIssueResponse> result) {
                if (result.isSuccess()) {
                    renderTable(result.getData());
                    updatePager(result.getData());
                }
            }
        });
    }

    private void updatePager(QueryProjectIssueResponse data) {
        lblCurrentPage.setText("第 " + currentPage + " 页");
        // 假设后端返回了总数数据（如果没有，请在 Response 中增加 totalCount）
        // lblPageInfo.setText("共 " + data.getTotalCount() + " 条");

        // 简单控制按钮状态
        btnPrev.setEnabled(currentPage > 1);
        // btnNext.setEnabled(data.getIssues().size() == pageSize);
    }

    private void initTableHeader() {
        // 1. 预先定义好各列宽度，这样 GWT 只会操作一次 <colgroup>
        HTMLTable.ColumnFormatter formatter = table.getColumnFormatter();
        formatter.setWidth(0, "50px");  // # ID列
        formatter.setWidth(1, "120px"); // 状态
        formatter.setWidth(2, "100px"); // 优先级
        formatter.setWidth(4, "120px"); // 负责人
        formatter.setWidth(5, "180px"); // 时间

        // 标题列不设宽度，利用 Flex 特性自适应
    }

    private void renderTable(QueryProjectIssueResponse data) {
        table.removeAllRows();
        initTableHeader(); // 重新加载表头和格式
        selectedRowIndex = -1; // 重置选中状态

        // 1. 渲染表头 (Row 0)
        String[] headers = {"#", "状态", "优先级", "标题", "负责人", "创建时间"};
        for (int i = 0; i < headers.length; i++) {
            table.setText(0, i, headers[i]);
        }
        // 注意：CSS 中已经通过 .table tr:first-child td 处理了表头样式

        // 2. 渲染数据行
        if (data.getIssues() == null || data.getIssues().isEmpty()) {
            table.setWidget(1, 0, new Label("暂无数据"));
            table.getFlexCellFormatter().setColSpan(1, 0, headers.length);
            return;
        }

        for (int i = 0; i < data.getIssues().size(); i++) {
            DevProjectIssueEntity issue = data.getIssues().get(i);
            int row = i + 1; // 数据从第一行开始

            // 设置行样式以激活 Hover 效果
            table.getRowFormatter().addStyleName(row, style.dataRow());
            // 填充数据
            int col = 0;
            table.setText(row, col++, issue.getCode() == null ? "-" : issue.getCode().toString());
            table.getColumnFormatter().setWidth(0, "60px");
            table.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER);

            IssueState stateEnum = IssueState.fromCode(issue.getState());
            table.setWidget(row, col++, createIconText(getStateIcon(stateEnum), stateEnum.getName()));
            // 2. 处理优先级图标列 (假设第2列)
            DevTaskPriority priorityEnum = DevTaskPriority.fromCode(issue.getPriority());
            table.setWidget(row, col++, createIconText(getPriorityIcon(priorityEnum), priorityEnum.getName()));
            table.setText(row, col++, issue.getName());

            //负责人
            // 在 renderTable 的循环中修改负责人列
            String avatarUrl = issue.getChargeAvatar();
            ImageResource defaultAvatar = AppResource.INSTANCE.avatar();

            // 如果有头像 URL 则用 URL，否则用资源文件里的默认头像
            Widget chargerWidget;
            if (StringUtil.isNotBlank(avatarUrl)) {
                Image avatar = new Image(avatarUrl);
                avatar.setPixelSize(24, 24);
                // 简单的圆角样式
                avatar.getElement().getStyle().setProperty("borderRadius", "50%");
                chargerWidget = createIconTextFromImage(avatar, issue.getChargeUserName());
            } else {
                chargerWidget = createIconText(AppResource.INSTANCE.emptyAvatar(), issue.getChargeUserName());
            }
            table.setWidget(row, col++, chargerWidget);

            table.setText(row, col++, StringUtil.formatDate(issue.getCreateTime()));

            // 将 Entity 绑定到 Row 上（可选，方便后续获取数据）
            table.getRowFormatter().getElement(row).setPropertyObject("data", issue);
        }
    }

    /**
     * 辅助方法：根据状态码获取对应的 ImageResource
     */
    private ImageResource getStateIcon(IssueState state) {
        switch (state) {
            case IS_OPEN:
                return AppResource.INSTANCE.statusOpen();
            case IS_CLOSED:
                return AppResource.INSTANCE.statusClosed();
            default:
                return AppResource.INSTANCE.statusCreated();
        }
    }

    /**
     * 辅助方法：根据优先级码获取对应的 ImageResource
     */
    private ImageResource getPriorityIcon(DevTaskPriority priority) {
        switch (priority) {
            case MEDIUM:
                return AppResource.INSTANCE.priorityMedium();
            case HIGH:
                return AppResource.INSTANCE.priorityHigh();
            case LOW:
            default:
                return AppResource.INSTANCE.priorityLow();
        }
    }

    // 重载一个辅助方法支持直接传入 Image 对象
    private Widget createIconTextFromImage(Image img, String text) {
        HTMLPanel panel = new HTMLPanel("");
        panel.setStyleName(style.item());
        img.getElement().getStyle().setMarginRight(8, Style.Unit.PX);
        panel.add(img);
        panel.add(new Label(text));
        return panel;
    }

    /**
     * 辅助方法：创建一个水平居中对齐的 [图标 + 文字] 组件
     */
    private Widget createIconText(ImageResource res, String text) {
        HTMLPanel panel = new HTMLPanel("");
        panel.setStyleName(style.item());
        Image img = new Image(res);
        img.setPixelSize(28, 28);
        img.getElement().getStyle().setMarginRight(5, Style.Unit.PX);
        panel.add(img);
        panel.add(new Label(text));
        return panel;
    }

    interface SStyle extends CssResource {

        String dataRow();

        String table();

        String selectedRow();

        String top();

        String item();

        String filter_label();

        String pager_box();
    }

    interface ProjectIssueFrameUiBinder extends UiBinder<DockLayoutPanel, ProjectIssueFrame> {
    }
}