package cn.mapway.gwt_template.client.workspace;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.resource.AppCss;
import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.user.UserIcon;
import cn.mapway.gwt_template.client.workspace.project.CreateProjectPanel;
import cn.mapway.gwt_template.client.workspace.project.DevProjectEditor;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.db.DevWorkspaceFolderEntity;
import cn.mapway.gwt_template.shared.rpc.user.ResourcePoint;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.AiAnchor;
import cn.mapway.ui.client.widget.AiLabel;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.buttons.EditButton;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

public class WorkspaceFolder extends CommonEventComposite implements IData<DevWorkspaceFolderEntity> {

    private static final WorkspaceFolderUiBinder ourUiBinder = GWT.create(WorkspaceFolderUiBinder.class);
    @UiField
    AiAnchor lbFolderName;
    @UiField
    AiButton btnCreateProject;
    @UiField
    FlexTable table;
    boolean empty = true;
    private DevWorkspaceFolderEntity folder;

    public WorkspaceFolder() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static int extractNumber(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        // 正则表达式：\d+ 匹配一个或多个数字
        RegExp regExp = RegExp.compile("\\d+");
        MatchResult matcher = regExp.exec(text);

        if (matcher != null && matcher.getGroupCount() > 0) {
            try {
                return Integer.parseInt(matcher.getGroup(0));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    @Override
    public DevWorkspaceFolderEntity getData() {
        return folder;
    }

    @Override
    public void setData(DevWorkspaceFolderEntity obj) {
        folder = obj;
        empty = true;
        applyFolderColor(); // 专门处理颜色逻辑
        toUI();
    }

    private void applyFolderColor() {
        String color = folder.getColor();
        if (StringUtil.isBlank(color)) {
            // 恢复默认样式
            lbFolderName.getElement().getParentElement().getStyle().clearBackgroundColor();
            lbFolderName.getElement().getStyle().setColor("#262626");
            return;
        }

        // 1. 改变 .top 区域背景
        // 注意：UiBinder中的 style 会被混淆，我们可以通过 parentElement 找到容器
        com.google.gwt.dom.client.Style topStyle = lbFolderName.getElement().getParentElement().getStyle();
        topStyle.setBackgroundColor(color);

        // 2. 增强视觉引导：设置一个稍微深一点的底部边框
        topStyle.setProperty("borderBottom", "2px solid rgba(0,0,0,0.1)");

        // 3. 智能文字颜色：如果背景太深，文字变白
        // 简单判断：如果不是非常浅的颜色，通常白色更好看
        lbFolderName.getElement().getStyle().setColor("#ffffff");

    }

    @Override
    protected void onLoad() {
        super.onLoad();
        boolean canCreate = ClientContext.get().isAssignResource(ResourcePoint.RP_PROJECT_CREATE.getCode());
        btnCreateProject.setEnabled(canCreate);
        if (!canCreate) {
            btnCreateProject.setTitle("您没有创建项目的权限");
        }
    }

    private void toUI() {
        if (StringUtil.isBlank(folder.getId())) {
            lbFolderName.setText(folder.getName());
        } else {
            lbFolderName.setText("\uD83D\uDCC2" + folder.getName());
        }

        table.removeAllRows(); // 使用 removeAllRows 比 clear 更彻底
        AppCss appCss = AppResource.INSTANCE.styles();
        // 设置表头样式
        table.addStyleName(appCss.table());
        table.getRowFormatter().addStyleName(0, appCss.tableHeader());

        int col = 0;
        table.setText(0, col++, "项目名称");
        table.setText(0, col++, "创建时间");
        table.setText(0, col++, "项目进度");
        table.setText(0, col++, "成员数量");
        table.setText(0, col++, "创建人");
        table.setText(0, col++, "操作");


        FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();
        formatter.getElement(0, 0).getStyle().setProperty("width", "40%"); // 名称占 40%
        formatter.getElement(0, 1).getStyle().setProperty("width", "120px"); // 时间
        formatter.getElement(0, 2).getStyle().setProperty("width", "120px"); // 进度

        int row = table.getRowCount(); // 此时应该是 1 (只有表头)
        table.setHTML(row, 0, "<div style='text-align:center; padding:30px; color:#bfbfbf;'>暂无项目，点击上方按钮创建</div>");
        table.getFlexCellFormatter().setColSpan(row, 0, 5); // 合并 5 列

    }

    public void updateProject(DevProjectEntity project) {
        for (int row = 1; row < table.getRowCount(); row++) {
            Widget widget = table.getWidget(row, 0);
            if (widget instanceof AiLabel) {
                AiLabel label = (AiLabel) widget;
                DevProjectEntity p = (DevProjectEntity) label.getData();
                if (p.getId().equals(project.getId())) {
                    updateRow(row, project);
                    break;
                }
            }
        }
    }

    public void addProject(DevProjectEntity project) {
        if (empty) {
            empty = false;
            table.removeRow(table.getRowCount() - 1);
        }
        int row = table.getRowCount();
        updateRow(row, project);
    }

    private void updateRow(int row, DevProjectEntity project) {
        int col = 0;
        AppCss css = AppResource.INSTANCE.styles();

        // 1. 项目名称区域
        HorizontalPanel hpName = new HorizontalPanel();
        hpName.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        hpName.setSpacing(4);

        AiLabel lbName = new AiLabel(project.getName());
        lbName.addStyleName(css.primaryText());
        lbName.setData(project);
        lbName.addClickHandler(event -> fireEvent(CommonEvent.selectEvent(project)));
        hpName.add(lbName);
        table.setWidget(row, col++, hpName);

        // 如果是模板，添加一个漂亮的标签
        if (project.getIsTemplate() != null && project.getIsTemplate()) {
            Label tag = new Label("模板");
            // 设置样式：背景浅蓝，文字深蓝，圆角小字体
            com.google.gwt.dom.client.Style tagStyle = tag.getElement().getStyle();
            tagStyle.setBackgroundColor("#e6f7ff");
            tagStyle.setColor("#1890ff");
            tagStyle.setProperty("border", "1px solid #91d5ff");
            tagStyle.setProperty("borderRadius", "2px");
            tagStyle.setProperty("padding", "0 6px");
            tagStyle.setProperty("fontSize", "11px");
            tagStyle.setProperty("lineHeight", "18px");
            hpName.add(tag);
        }

        // 2. 创建时间：使用更淡的颜色
        Label lbTime = new Label(StringUtil.formatDate(project.getCreateTime(), "yyyy-MM-dd"));
        lbTime.addStyleName(css.secondaryText());
        table.setWidget(row, col++, lbTime);

        // 3. 项目进度：可视化
        if (project.getIsTemplate() != null && project.getIsTemplate()) {
            table.setText(row, col++, "-"); // 模板不显示进度
        } else {
            HTML progressHtml = new HTML(renderProgressBar(project.getProgress()));
            table.setWidget(row, col++, progressHtml);
        }

        // 4. 成员数量
        table.setText(row, col++, project.getMemberCount() + " 人");

        // 5. 创建人（包含头像）
        UserIcon userIcon = new UserIcon();
        userIcon.setUserInformation(project.getUserId(), project.getCreateUserName(), project.getCreateUserAvatar()).setImageSize(24, 24);
        table.setWidget(row, col++, userIcon);

        EditButton editButton = new EditButton();
        editButton.setData(project);
        editButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                editProject(project);
            }

        });
        table.setWidget(row, col++, editButton);

        // 行样式美化
        HTMLTable.RowFormatter rowFormatter = table.getRowFormatter();
        rowFormatter.addStyleName(row, css.tableRow());
        rowFormatter.getElement(row).setAttribute("alt", project.getId());
    }

    private String renderProgressBar(String progress) {
        int val = extractNumber(progress);
        String color = val >= 100 ? "#52c41a" : "#1890ff";
        // 使用 flex 布局让进度条和文字垂直排列并居中
        return "<div style='display:flex; flex-direction:column; gap:4px;'>" +
                "<div style='width:100px; height:6px; background:#f0f0f0; border-radius:3px; overflow:hidden;'>" +
                "<div style='width:" + val + "%; height:100%; background:" + color + "; transition: width 0.3s;'></div>" +
                "</div>" +
                "<span style='font-size:11px; color:#8c8c8c; line-height:1;'>" + progress + "</span>" +
                "</div>";
    }

    @UiHandler("btnCreateProject")
    public void btnCreateProjectClick(ClickEvent event) {
        createProject();
    }

    private void editProject(DevProjectEntity project) {
        Dialog<DevProjectEditor> dialog = DevProjectEditor.getDialog(true);
        dialog.addCommonHandler(event -> {
            if (event.isUpdate()) {
                updateProject(event.getValue());
                dialog.hide();
            } else if (event.isClose()) {
                dialog.hide();
            }
        });
        dialog.getContent().setData(project);
        dialog.center();
    }

    private void createProject() {
        Popup<CreateProjectPanel> dialog = CreateProjectPanel.getDialog(true);
        dialog.addCommonHandler(event -> {
            if (event.isReload()) {
                //addProject(event.getValue());
                //重新加载吧
                fireEvent(CommonEvent.reloadEvent(null));
                dialog.hide();
            } else if (event.isClose()) {
                dialog.hide();
            }
        });
        dialog.getContent().loadTemplates();
        String folderId = folder.getId();
        if (AppConstant.TEMP_WORKSPACE_FOLDER_ID.equals(folderId)) {
            folderId = null;
        }
        dialog.getContent().setCreateParameter(folder.getWorkspaceId(), folderId);
        dialog.center();
    }

    interface WorkspaceFolderUiBinder extends UiBinder<HTMLPanel, WorkspaceFolder> {
    }
}