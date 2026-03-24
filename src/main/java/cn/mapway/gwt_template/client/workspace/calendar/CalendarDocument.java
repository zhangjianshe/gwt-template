package cn.mapway.gwt_template.client.workspace.calendar;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.workspace.calendar.editor.MeetingEditor;
import cn.mapway.gwt_template.client.workspace.calendar.events.ProjectCalendarHitResult;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTaskResponse;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.*;
import cn.mapway.ui.client.mvc.Rect;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.rpc.AsyncCallback;
import elemental2.core.JsDate;
import elemental2.dom.DomGlobal;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CalendarDocument {
    final JsDate alignedStartTime = new JsDate();
    final List<MeetingNode> allNodes = new ArrayList<>();
    final List<MeetingNode> selectedNodes = new ArrayList<>();
    private final TimeSpaceView projector;
    @Getter
    boolean valid;
    @Getter
    String errorMessage = "";
    @Getter
    @Setter
    ProjectCalendar chart;
    String projectId;
    @Getter
    boolean readOnly;
    /**
     * 画布当前的开始事件
     */
    double startTimeMillis = System.currentTimeMillis();
    double totalHeight = 0;
    Animation currentAnimation = null;
    Size TEMP_SCREEN_SIZE = new Size(0, 0);
    Size TEMP_WORLD_SIZE = new Size(0, 0);
    Size TEMP_LOCATION_IN_WORLD = new Size(0, 0);
    MeetingNode hoverNode;
    @Getter
    private double dayWidth = 40.0; // 默认一天 40 像素
    @Getter
    private double scrollTop = 0;

    public CalendarDocument() {
        projector = new TimeSpaceView();
        projector.setDayWidth(getDayWidth());
    }

    // 在 offsetTimeline 或 handleZoom 中更新状态后调用
    private void syncProjector() {
        projector.setDayWidth(this.dayWidth);
        // 将当前视图的起始时间(ms)和滚动高度(px)传给投影器
        projector.setViewOffset(this.startTimeMillis, this.scrollTop);

        double worldWidth = projector.pxToDuration(chart.getOffsetWidth());
        double worldHeight = projector.pxToDuration(chart.getOffsetHeight());
        projector.setViewSize(worldWidth, worldHeight);
    }

    public Rect getViewRect() {
        return projector.getViewRect();
    }

    /**
     * 复杂的布局算法
     * 每一天 为 24*60*60= 86400秒 如果我们将通过缩放映射为屏幕上的像素坐标+
     */
    public void reLayout() {
        int ITEM_HEIGHT = 40;
        // 注意：这里 Rect 存储的是“世界坐标”
        // X 是时间戳 (ms)，Y 是相对于文档顶部的像素
        double currentY = 10 + getTopHeight();

        for (MeetingNode node : allNodes) {
            node.getRect().setY(currentY);
            node.getRect().setHeight(ITEM_HEIGHT);
            node.reLayout();
            currentY += ITEM_HEIGHT;
        }
        this.totalHeight = currentY;
        chart.redraw();
    }

    public void clear() {
        allNodes.clear();
    }

    private void setError(String msg) {
        valid = false;
        this.errorMessage = StringUtil.isBlank(msg) ? "NO MESSAGE" : msg;
        DomGlobal.console.log(errorMessage);
        chart.redraw();
    }

    public void loadDocument(String projectId) {
        if (StringUtil.isBlank(projectId)) {
            setError("没有提供项目ID");
        }
        this.projectId = projectId;
        QueryProjectTaskRequest request = new QueryProjectTaskRequest();
        request.setProjectId(projectId);
        request.setCatalog(DevTaskCatalog.DTC_MEETING.getCode());
        AppProxy.get().queryProjectTask(request, new AsyncCallback<RpcResult<QueryProjectTaskResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                readOnly = true;
                DomGlobal.console.log("loading " + projectId + " " + caught.getMessage());
                setError(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryProjectTaskResponse> result) {
                if (result.isSuccess()) {
                    valid = true;
                    allNodes.clear();
                    CommonPermission permission = CommonPermission.from(result.getData().getUserPermission());
                    readOnly = !(permission.isOwner() || permission.isSecretary());
                    for (DevProjectTaskEntity meeting : result.getData().getRootTasks()) {
                        MeetingNode meetingNode = new MeetingNode(meeting);
                        allNodes.add(meetingNode);
                    }
                    if (allNodes.isEmpty()) {
                        setError("目前还没有项目日历信息, 回车键 创建，或者 / 寻找帮助!");
                        return;
                    }
                    buildAndLayout();
                } else {
                    readOnly = true;
                    setError(result.getMessage());
                }
            }
        });
    }

    /**
     * 会议没有父子关系 是一个按照时间的顺序组织
     */
    private void buildAndLayout() {
        startTimeMillis = System.currentTimeMillis()
                - getTimeBySpan((chart.getOffsetWidth() / 2.));
        syncProjector();
        reLayout();
    }

    public void addMeeting() {
        if (isReadOnly()) {
            return;
        }
        DevProjectTaskEntity meeting = new DevProjectTaskEntity();
        meeting.setCatalog(DevTaskCatalog.DTC_MEETING.getCode());
        meeting.setName("会议主题");
        meeting.setProjectId(projectId);
        meeting.setParentId("");
        meeting.setKind(DevTaskKind.DTK_EPIC.getCode());
        meeting.setPriority(DevTaskPriority.MEDIUM.getCode());
        meeting.setCharger(Long.parseLong(ClientContext.get().getUserInfo().getId()));
        meeting.setStartTime(new Timestamp(System.currentTimeMillis()));
        meeting.setEstimateTime(new Timestamp(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000));
        meeting.setInitExpand(true);
        meeting.setStatus(DevTaskStatus.DTS_CREATED.getCode());
        meeting.setRank(1.0);
        meeting.setSummary("{}");
        editMeeting(meeting);
    }

    private void editMeeting(DevProjectTaskEntity meeting) {
        if (StringUtil.isBlank(meeting.getProjectId())) {
            DomGlobal.console.log("no project id set");
            return;
        }
        Popup<MeetingEditor> dialog = MeetingEditor.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent commonEvent) {
                if (commonEvent.isOk()) {
                    dialog.hide();
                    loadDocument(projectId);
                } else if (commonEvent.isClose()) {
                    dialog.hide();
                }
            }
        });
        dialog.getContent().setData(meeting);
        dialog.center();
    }

    public double getTopHeight() {
        return 64;
    }

    /**
     * 获取对齐到当天 0 点的起始日期
     * 修正：考虑本地时区偏移，确保日期数字显示正确
     */
    public long getAlignedStartTime() {
        alignedStartTime.setTime(this.startTimeMillis);
        alignedStartTime.setHours(0, 0, 0, 0);
        return (long) alignedStartTime.getTime();
    }

    // 替代原有的 getScreenX
    public double getScreenX(double dateMillis) {
        TEMP_WORLD_SIZE.set(dateMillis, 0); // Y 在这里不重要
        TEMP_SCREEN_SIZE.set(0, 0);
        projector.project(TEMP_WORLD_SIZE, TEMP_SCREEN_SIZE);
        return TEMP_SCREEN_SIZE.x;
    }

    // 替代原有的 getTimeByX
    public double getTimeByX(double x) {
        TEMP_SCREEN_SIZE.set(x, 0);
        TEMP_WORLD_SIZE.set(0, 0);
        projector.unProject(TEMP_SCREEN_SIZE, TEMP_WORLD_SIZE);
        return TEMP_WORLD_SIZE.x;
    }

    // 获取不带左侧偏移的纯跨度像素转时间
    public double getTimeBySpan(double pixelSpan) {
        return projector.pxToDuration(pixelSpan);
    }

    public boolean hitTest(ProjectCalendarHitResult result, Size canvasLoc) {
        screenToWorld(canvasLoc, TEMP_LOCATION_IN_WORLD);
        for (MeetingNode meetingNode : allNodes) {
            boolean b = meetingNode.hitTest(this, result, TEMP_LOCATION_IN_WORLD);
            if (b) {
                return true;
            }
        }
        result.none();
        return false;
    }

    /**
     * 水平平移和垂直滚动
     * 优化：水平移动不再触发 reLayout
     */
    public void offsetTimeline(double deltaX, double deltaY) {
        // 1. 处理垂直滚动（像素级）
        this.scrollTop += deltaY;
        double maxScroll = Math.max(0, totalHeight - (chart.getOffsetHeight() - getTopHeight()));
        if (this.scrollTop < 0) this.scrollTop = 0;
        if (this.scrollTop > maxScroll) this.scrollTop = maxScroll;

        // 2. 处理水平滚动 (通过 projector 将像素位移转为时间位移)
        double deltaMillis = projector.pxToDuration(deltaX);
        this.startTimeMillis -= deltaMillis;

        // 3. 核心优化：
        // 垂直滚动 deltaY != 0 时，如果涉及行变动，可能需要 reLayout（目前你的 Y 是固定的，其实也不需要）
        // 水平平移 deltaX 时，绝对坐标不变，只需同步投影器状态并重绘
        syncProjector();
        chart.redraw();
    }

    /**
     * 以鼠标位置为中心进行缩放
     * 逻辑：锁定鼠标下的世界时间点，缩放后通过投影器反向计算新的视图起点
     */
    public void handleZoom(double deltaY, double mouseX) {
        // 1. 记录缩放前的鼠标指向的绝对时间（世界坐标）
        double mouseWorldTime = getTimeByX(mouseX);

        // 2. 计算并限制缩放比例
        double zoomSpeed = Math.min(Math.abs(deltaY) / 600.0, 0.2);
        double zoomFactor = (deltaY < 0) ? (1 + zoomSpeed) : (1 - zoomSpeed);
        double newDayWidth = dayWidth * zoomFactor;

        if (newDayWidth < 0.3) newDayWidth = 0.3;
        if (newDayWidth > 2000.0) newDayWidth = 2000.0;
        if (newDayWidth == dayWidth) return;

        // 3. 应用新缩放
        this.dayWidth = newDayWidth;
        projector.setDayWidth(this.dayWidth);

        // 4. 关键：重新对齐视图起点
        // 我们希望缩放后，mouseWorldTime 依然投影在屏幕的 mouseX 处
        // 根据公式：mouseX = (mouseWorldTime - startTimeMillis) * ratio
        // 反推：startTimeMillis = mouseWorldTime - (mouseX / ratio)
        this.startTimeMillis = mouseWorldTime - projector.pxToDuration(mouseX);

        // 5. 更新投影器状态并重绘
        syncProjector();
        // 如果缩放会引起元素垂直高度或排版变化，则调用 reLayout，否则只需 redraw
        chart.redraw();
    }

    /**
     * 将屏幕上的鼠标位置转换为世界坐标系中的位置
     *
     * @param screenLoc 鼠标点击的屏幕像素坐标
     * @return 转换后的世界坐标 (x 为时间戳ms, y 为文档内的绝对像素高度)
     */
    public void screenToWorld(Size screenLoc, Size worldLoc) {
        // 逆向投影
        projector.unProject(screenLoc, worldLoc);
    }

    public void scrollToNow() {
        double targetStartTime = System.currentTimeMillis()
                - getTimeBySpan((chart.getOffsetWidth() / 2.));
        scrollToTimestamp(targetStartTime, scrollTop);
    }

    /**
     * 滚动到当前的时间线
     */
    public void scrollToTimestamp(double targetStartTime, double targetScrollTop) {
        // 1. 如果有正在进行的动画，立即停止它
        if (currentAnimation != null) {
            currentAnimation.cancel();
        }

        // 3. 记录动画起始状态
        final double startS = scrollTop;
        final double deltaS = targetScrollTop - startS;
        final double startT = startTimeMillis;
        final double deltaT = targetStartTime - startT;

        // 4. 创建并赋值给成员变量
        currentAnimation = new Animation() {
            @Override
            protected void onUpdate(double progress) {
                scrollTop = startS + (deltaS * progress);
                startTimeMillis = (startT + (deltaT * progress));
                syncProjector();
                chart.redraw();
            }

            @Override
            protected double interpolate(double progress) {
                // Standard Ease-out: 1 - (1 - x)^3
                return 1.0 - Math.pow(1.0 - progress, 3);
            }

            @Override
            protected void onComplete() {
                // 动画正常结束，清除引用
                currentAnimation = null;

                // 最终位置补偿
                scrollTop = startS + deltaS;
                startTimeMillis = (startT + deltaT);
                syncProjector();
                chart.redraw();
            }

            @Override
            protected void onCancel() {
                // 动画被取消时，也清除引用
                currentAnimation = null;
                syncProjector();
                chart.redraw();
            }
        };

        // 启动新动画
        currentAnimation.run(400); // 稍微加快到 400ms 增加响应感
    }

    public void projectToScreen(Rect worldRect, Rect screenRect) {
        projector.projectRect(worldRect, screenRect);
    }

    public void clearHoverNode() {
        if (hoverNode != null) {
            hoverNode.clearState();
        }
        hoverNode = null;
    }

    public void setHoverNode(MeetingNode node) {
        if (node != null) {
            this.hoverNode = node;
        }
    }

    public void updateMeetingTime(MeetingNode node, double oldStart, double oldEstimate) {
        DevProjectTaskEntity oldEntity = node.getMeeting();
        if (oldEntity == null) {
            return;
        }
        DevProjectTaskEntity temp = new DevProjectTaskEntity();
        temp.setId(oldEntity.getId());
        temp.setStartTime(oldEntity.getStartTime());
        temp.setEstimateTime(oldEntity.getEstimateTime());
        temp.setProjectId(oldEntity.getProjectId());
        UpdateProjectTaskRequest req = new UpdateProjectTaskRequest();
        req.setProjectTask(temp);
        AppProxy.get().updateProjectTask(req, new AsyncCallback<RpcResult<UpdateProjectTaskResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                oldEntity.setStartTime(new Timestamp((long) oldStart));
                oldEntity.setEstimateTime(new Timestamp((long) oldEstimate));
                node.reLayout();
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateProjectTaskResponse> result) {
                if (result.isSuccess()) {

                } else {
                    oldEntity.setStartTime(new Timestamp((long) oldStart));
                    oldEntity.setEstimateTime(new Timestamp((long) oldEstimate));
                    node.reLayout();
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    public double getSnapMs() {
        return 10 * 60 * 1000L;
    }

    public void appendSelect(MeetingNode node, boolean clear) {
        if (clear) {
            for (MeetingNode n : selectedNodes) {
                n.setSelected(false);
            }
            selectedNodes.clear();
        }
        if (node != null) {
            node.setSelected(true);
            selectedNodes.add(node);
        }
    }

    public void fireSelectFirst() {
        if (!selectedNodes.isEmpty()) {
            chart.fireEvent(CommonEvent.selectEvent(selectedNodes.get(0).getMeeting()));
        }
    }

    public void updateMeetingLocal(DevProjectTaskEntity meeting) {
        if (meeting == null) {
            return;
        }
        for (MeetingNode node : allNodes) {
            if (node.getMeeting().getId().equals(meeting.getId())) {
                node.meeting = meeting;
                break;
            }
        }
        chart.redraw();
    }

    public void selectNone() {
        appendSelect(null, true);
        chart.fireEvent(CommonEvent.selectEvent(null));
    }
}
