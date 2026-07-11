package cn.mapway.gwt_template.client.workspace.calendar;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.workspace.project.DevProjectEditor;
import cn.mapway.gwt_template.client.workspace.project.ProjectCard;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevProjectRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevProjectResponse;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTaskResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.*;
import cn.mapway.ui.client.mvc.IToolsProvider;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.panel.MessagePanel;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 项目日历
 */
public class ProjectCalendarWidget extends CommonEventComposite implements IToolsProvider, RequiresResize, IData<String> {
    private static final ProjectCalendarWidgetUiBinder ourUiBinder = GWT.create(ProjectCalendarWidgetUiBinder.class);
    @UiField
    ScrollPanel root;
    @UiField
    HTMLPanel eventList;
    @UiField
    ProjectCard projectCard;
    @UiField
    HorizontalPanel tools;
    @UiField
    AiButton btnCreate;
    @UiField
    AiButton btnEdit;
    boolean readOnly = true;
    private final CommonEventHandler meetingItemHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent event) {
            if (event.isEdit()) {
                MeetingItem source = (MeetingItem) event.getSource();
                DevProjectTaskEntity meet = event.getValue();
                edit(source, meet);
            }
        }
    };
    private String projectId;

    public ProjectCalendarWidget() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    private void edit(MeetingItem source, DevProjectTaskEntity meet) {
        Dialog<MeetingDialog> dialog = MeetingDialog.getDialog(true);
        dialog.addCommonHandler(event -> {
            if (event.isUpdate()) {
                source.setData(event.getValue());
            }
            dialog.hide();
        });
        dialog.getContent().edit(meet);
        dialog.getContent().enableEdit(!readOnly);
        dialog.center();
    }

    @Override
    public String getData() {
        return projectId;
    }

    @Override
    public void setData(String projectId) {
        this.projectId = projectId;
        loadProject(projectId);
        loadEvents();

    }

    private void loadProject(String projectId) {
        QueryDevProjectRequest request = new QueryDevProjectRequest();
        request.setProjectId(projectId);
        AppProxy.get().queryDevProject(request, new AsyncCallback<RpcResult<QueryDevProjectResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryDevProjectResponse> result) {
                if (result.isSuccess()) {
                    DevProjectEntity projectEntity = result.getData().getProjects().get(0);
                    projectCard.setData(projectEntity);
                    CommonPermission commonPermission = CommonPermission.from(projectEntity.getCurrentUserPermission());
                    btnEdit.setEnabled(commonPermission.isSuper());
                    btnCreate.setEnabled(commonPermission.isSuper() || commonPermission.isSecretary());
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());

                }
            }
        });
    }


    void setError(String message) {
        MessagePanel messagePanel = new MessagePanel();
        messagePanel.setText(message);
        messagePanel.setHeight("200px");
        eventList.add(messagePanel, "");
    }

    private void loadEvents() {
        QueryProjectTaskRequest request = new QueryProjectTaskRequest();
        request.setProjectId(projectId);
        request.setCatalog(DevTaskCatalog.DTC_MEETING.getCode());
        eventList.clear();
        AppProxy.get().queryProjectTask(request, new AsyncCallback<RpcResult<QueryProjectTaskResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                setError(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryProjectTaskResponse> result) {
                if (result.isSuccess()) {
                    CommonPermission permission = CommonPermission.from(result.getData().getUserPermission());
                    readOnly = !(permission.isOwner() || permission.isSecretary());
                    btnCreate.setEnabled(!readOnly);
                    List<DevProjectTaskEntity> rootTasks = result.getData().getRootTasks();
                    Collections.sort(rootTasks, Comparator.comparing(DevProjectTaskEntity::getStartTime).reversed());
                    for (DevProjectTaskEntity meeting : rootTasks) {
                        MeetingItem item = new MeetingItem();
                        item.addCommonHandler(meetingItemHandler);
                        item.setData(meeting);
                        item.setReadonly(readOnly);
                        eventList.add(item);
                    }
                    if (rootTasks.isEmpty()) {
                        setError("目前还没有项目日历信息, 回车键 创建，或者 / 寻找帮助!");
                    }
                } else {
                    setError(result.getMessage());
                }
            }
        });
    }

    public void setFocus(boolean b) {

    }

    @Override
    public void onResize() {
        root.onResize();
    }

    @Override
    public Widget getTools() {
        return tools;
    }

    @UiHandler("btnCreate")
    public void btnCreateClick(ClickEvent event) {
        DevProjectTaskEntity meeting = new DevProjectTaskEntity();
        meeting.setCatalog(DevTaskCatalog.DTC_MEETING.getCode());
        meeting.setName("内容主题");
        meeting.setProjectId(projectId);
        meeting.setParentId("");
        meeting.setKind(DevTaskKind.DTK_EPIC.getCode());
        meeting.setPriority(DevTaskPriority.MEDIUM.getCode());
        meeting.setCharger(Long.parseLong(ClientContext.get().getUserInfo().getId()));
        meeting.setStartTime(new Timestamp(System.currentTimeMillis()));
        meeting.setEstimateTime(new Timestamp(meeting.getStartTime().getTime() + 4 * 60 * 60 * 1000));
        meeting.setInitExpand(true);
        meeting.setStatus(DevTaskStatus.DTS_CREATED.getCode());
        meeting.setRank(1.0);
        meeting.setSummary("{}");
        editMeeting(meeting);
    }

    @UiHandler("btnEdit")
    public void btnEditClick(ClickEvent event) {
        Dialog<DevProjectEditor> dialog = DevProjectEditor.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isUpdate()) {
                    loadProject(projectId);
                }
                dialog.hide();
            }
        });
        dialog.getContent().setData(projectCard.getData());

        dialog.center();
    }

    private void editMeeting(DevProjectTaskEntity meet) {
        Dialog<MeetingDialog> dialog = MeetingDialog.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isUpdate()) {
                    loadEvents();
                }
                dialog.hide();
            }
        });
        dialog.center();
        dialog.getContent().edit(meet);
        dialog.getContent().enableEdit(!readOnly);
    }

    interface ProjectCalendarWidgetUiBinder extends UiBinder<ScrollPanel, ProjectCalendarWidget> {
    }
}