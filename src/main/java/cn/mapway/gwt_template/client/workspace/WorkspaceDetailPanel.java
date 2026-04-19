package cn.mapway.gwt_template.client.workspace;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.db.DevWorkspaceEntity;
import cn.mapway.gwt_template.shared.db.DevWorkspaceFolderEntity;
import cn.mapway.gwt_template.shared.rpc.project.DeleteDevWorkspaceFolderRequest;
import cn.mapway.gwt_template.shared.rpc.project.DeleteDevWorkspaceFolderResponse;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevProjectRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevProjectResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.IToolsProvider;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.Colors;
import cn.mapway.ui.client.util.IEachElement;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.buttons.DeleteButton;
import cn.mapway.ui.client.widget.buttons.EditButton;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.list.CommonList;
import cn.mapway.ui.client.widget.list.CommonListItem;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import elemental2.promise.IThenable;
import org.jspecify.annotations.Nullable;

import java.sql.Timestamp;
import java.util.*;

/**
 * 工作空间详情
 */
public class WorkspaceDetailPanel extends CommonEventComposite implements IToolsProvider, RequiresResize, IData<DevWorkspaceEntity> {
    private static final WorkspaceDetailPanelUiBinder ourUiBinder = GWT.create(WorkspaceDetailPanelUiBinder.class);
    @UiField
    DockLayoutPanel root;
    @UiField
    AiButton btnCreateDir;
    @UiField
    CommonList folders;
    @UiField
    WorkspaceFolder workspaceFolder;
    Map<String, List<DevProjectEntity>> projectsMaper = new HashMap<>();
    String initShowFolder = "";
    private DevWorkspaceEntity workspace;

    public WorkspaceDetailPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static String getYearQuarter(Date date) {
        if (date == null) date = new Date();
        // 获取年份
        int year = Integer.parseInt(DateTimeFormat.getFormat("yyyy").format(date));
        // 获取月份 (1-12)
        int month = Integer.parseInt(DateTimeFormat.getFormat("MM").format(date));
        int quarter = (month - 1) / 3 + 1;
        return year + "年第" + quarter + "季度工作项目";
    }

    @Override
    public DevWorkspaceEntity getData() {
        return workspace;
    }

    @Override
    public void setData(DevWorkspaceEntity obj) {
        workspace = obj;
        toUI();
    }

    private void toUI() {
        QueryDevProjectRequest request = new QueryDevProjectRequest();
        request.setWorkspaceId(workspace.getId());
        AppProxy.get().queryDevProject(request, new AsyncCallback<RpcResult<QueryDevProjectResponse>>() {
            @Override
            public void onFailure(Throwable caught) {

            }

            @Override
            public void onSuccess(RpcResult<QueryDevProjectResponse> result) {
                renderProjects(result.getData());
            }
        });
    }

    private void renderProjects(QueryDevProjectResponse response) {

        folders.clear();
        projectsMaper.clear();
        workspaceFolder.setFolders(response.getFolders());

        List<DevWorkspaceFolderEntity> responseFolders = response.getFolders();
        DevWorkspaceFolderEntity temp = new DevWorkspaceFolderEntity();
        temp.setName("未分类项目");
        temp.setWorkspaceId(workspace.getId());
        temp.setChildren(new ArrayList<>());
        temp.setColor("");
        temp.setId(AppConstant.TEMP_WORKSPACE_FOLDER_ID);
        temp.setCreateTime(new Timestamp(System.currentTimeMillis()));
        responseFolders.add(temp);


        for (DevWorkspaceFolderEntity folder : responseFolders) {
            CommonListItem commonListItem = folders.addItem(Fonts.FOLDER, folder.getName(), folder);
            if (!StringUtil.isBlank(folder.getId()) && !folder.getId().equals(AppConstant.TEMP_WORKSPACE_FOLDER_ID)) {
                DeleteButton deleteButton = new DeleteButton();
                deleteButton.setData(folder);
                deleteButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        event.preventDefault();
                        event.stopPropagation();
                        confirmDelete(folder);
                    }
                });
                commonListItem.appendRightWidget(deleteButton);
                EditButton editButton = new EditButton();
                editButton.setData(folder);
                editButton.addClickHandler(event -> {
                    event.stopPropagation();
                    event.preventDefault();
                    editFolder(commonListItem, folder);
                });
                commonListItem.appendRightWidget(editButton);
            }
            Style style = commonListItem.getLabel().getElement().getStyle();
            style.setColor(folder.getColor());
        }

        Collections.sort(responseFolders, Comparator.comparing(DevWorkspaceFolderEntity::getCreateTime));


        for (DevProjectEntity project : response.getProjects()) {
            if (StringUtil.isBlank(project.getFolderId()) || project.getFolderId().equals(AppConstant.TEMP_WORKSPACE_FOLDER_ID)) {
                project.setFolderId(AppConstant.TEMP_WORKSPACE_FOLDER_ID);
            }
            List<DevProjectEntity> container = projectsMaper.computeIfAbsent(project.getFolderId(), k -> new ArrayList<>());
            container.add(project);
        }
        if (StringUtil.isNotBlank(initShowFolder)) {
            folders.eachItem(new IEachElement<CommonListItem>() {
                @Override
                public boolean each(CommonListItem e) {
                    DevWorkspaceFolderEntity folder = (DevWorkspaceFolderEntity) e.getData();
                    if (folder.getId().equals(initShowFolder)) {
                        folders.selectItem(e, true);
                        return false;
                    }
                    return true;
                }
            });
        } else {
            folders.eachItem(new IEachElement<CommonListItem>() {
                @Override
                public boolean each(CommonListItem e) {
                    folders.selectItem(e, true);
                    return false;
                }
            });
        }
    }

    private void confirmDelete(DevWorkspaceFolderEntity folder) {
        String msg = "<div style='font-weight:bold;color=red;padding-bottom:15px;'>删除项目文件夹" + folder.getName() + "?</div><p> 删除后 其下的所有项目将会转移到临时文件夹中";
        ClientContext.get().confirmDelete(msg).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
            @Override
            public @Nullable IThenable<Object> onInvoke(Void p0) {
                doDeleteFolder(folder);
                return null;
            }
        });
    }

    private void doDeleteFolder(DevWorkspaceFolderEntity folder) {
        DeleteDevWorkspaceFolderRequest request = new DeleteDevWorkspaceFolderRequest();
        request.setFolderId(folder.getId());
        AppProxy.get().deleteDevWorkspaceFolder(request, new AsyncCallback<RpcResult<DeleteDevWorkspaceFolderResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<DeleteDevWorkspaceFolderResponse> result) {
                if (result.isSuccess()) {
                    setData(workspace);
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    private void editFolder(CommonListItem commonListItem, DevWorkspaceFolderEntity folder) {
        if (StringUtil.isBlank(folder.getId())) {
            return;
        }
        Dialog<WorkspaceFolderEditor> dialog = WorkspaceFolderEditor.getDialog(true);
        dialog.addCommonHandler(event -> {
            if (event.isUpdate()) {
                DevWorkspaceFolderEntity folder1 = event.getValue();
                commonListItem.setText(folder1.getName());
                commonListItem.getLabel().getElement().getStyle().setColor(folder1.getColor());
            }
            dialog.hide();
        });
        dialog.getContent().setData(folder);
        dialog.center();

    }

    @Override
    public void onResize() {
        root.onResize();
    }

    @Override
    public Widget getTools() {
        return new Label("");
    }

    @UiHandler("btnCreateDir")
    public void btnCreateDirClick(ClickEvent event) {
        Dialog<WorkspaceFolderEditor> dialog = WorkspaceFolderEditor.getDialog(true);
        dialog.addCommonHandler(event1 -> {
            if (event1.isUpdate()) {
                toUI();
            }
            dialog.hide();
        });
        DevWorkspaceFolderEntity newFolder = new DevWorkspaceFolderEntity();
        newFolder.setWorkspaceId(workspace.getId());
        newFolder.setName(StringUtil.formatDate(new Date(), getYearQuarter(new Date())));
        newFolder.setColor(Colors.randomColor());
        dialog.getContent().setData(newFolder);
        dialog.center();
    }

    @UiHandler("folders")
    public void foldersCommon(CommonEvent event) {
        if (event.isSelect()) {
            DevWorkspaceFolderEntity folder = event.getValue();
            workspaceFolder.setData(folder);
            List<DevProjectEntity> projects = projectsMaper.get(folder.getId());
            if (projects != null) {
                for (DevProjectEntity project : projects) {
                    workspaceFolder.addProject(project);
                }
            }
        }
    }

    @UiHandler("workspaceFolder")
    public void workspaceFolderCommon(CommonEvent event) {
        if (event.isSelect()) {
            DevProjectEntity project = event.getValue();
            fireEvent(CommonEvent.selectEvent(project));
        } else if (event.isReload()) {
            initShowFolder = workspaceFolder.getData().getId();
            setData(workspace);
        }
    }

    interface WorkspaceDetailPanelUiBinder extends UiBinder<DockLayoutPanel, WorkspaceDetailPanel> {
    }


}