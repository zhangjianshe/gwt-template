package cn.mapway.gwt_template.client.workspace.calendar;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.workspace.calendar.editor.MeetingEditor;
import cn.mapway.gwt_template.client.workspace.calendar.events.ProjectCalendarHitResult;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTaskResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskCatalog;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskKind;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskPriority;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskStatus;
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

import static cn.mapway.gwt_template.client.workspace.gantt.CalendarTimes.MS_PER_DAY;

public class CalendarDocument {
    final JsDate alignedStartTime = new JsDate();
    final List<MeetingNode> allNodes = new ArrayList<>();
    final List<MeetingNode> drawingItems = new ArrayList<>();
    @Getter
    boolean valid;
    @Getter
    String errorMessage = "";
    @Getter
    @Setter
    ProjectCalendar chart;
    String projectId;
    /**
     * 画布当前的开始事件
     */
    double startTimeMillis = System.currentTimeMillis();
    double totalHeight = 0;
    Animation currentAnimation = null;
    @Getter
    private double dayWidth = 40.0; // 默认一天 40 像素
    @Getter
    private double scrollTop = 0;

    /**
     * 复杂的布局算法
     */
    public void reLayout() {
        int ITEM_HEIGHT = 40;
        double top = 10 + getTopHeight();
        drawingItems.clear();
        for (MeetingNode node : allNodes) {
            node.getRect().set(0, top, 0, ITEM_HEIGHT);
            drawingItems.add(node);
            top += ITEM_HEIGHT;
        }
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
        DomGlobal.console.log("loading " + projectId);
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
                DomGlobal.console.log("loading " + projectId + " " + caught.getMessage());
                setError(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryProjectTaskResponse> result) {
                if (result.isSuccess()) {
                    valid = true;
                    DomGlobal.console.log("loading " + projectId + " success");
                    allNodes.clear();
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
        reLayout();
    }

    public void addMeeting() {
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

    // 将日期转换为相对于左侧面板边缘的像素偏移
    public double getXByDate(double dateMillis) {
        return (dateMillis - startTimeMillis) / MS_PER_DAY * dayWidth;
    }

    // 对应的逆运算：将屏幕 X 坐标转回时间戳
    public double getTimeByX(double x) {
        double days = (x) / dayWidth;
        return startTimeMillis + (days * MS_PER_DAY);
    }

    // 获取不带左侧偏移的纯跨度像素转时间
    public double getTimeBySpan(double span) {
        return (span / dayWidth) * MS_PER_DAY;
    }

    public void hitTest(ProjectCalendarHitResult result, Size current) {
        result.none();
    }

    public void offsetTimeline(double deltaX, double deltaY) {
        // 1. 处理垂直滚动
        this.scrollTop += deltaY;

        // 边界检查：不要滚出任务列表底部

        double maxScroll = Math.max(0, totalHeight - (chart.getOffsetHeight() - getTopHeight()));
        if (this.scrollTop < 0) this.scrollTop = 0;
        if (this.scrollTop > maxScroll) this.scrollTop = maxScroll;

        // 2. 处理水平滚动 (时间轴平移)
        // 关键：deltaX 是像素，通过 double 运算转为时间偏移
        double msPerPixel = MS_PER_DAY / dayWidth;
        double deltaMillis = deltaX * msPerPixel;

        // 更新起始时间 (double 类型)
        this.startTimeMillis -= deltaMillis;

        // 3. 触发重绘
        reLayout(); // 重新计算所有 Item 的 Y 坐标（受 scrollTop 影响）
        chart.redraw();
    }

    /**
     * 以鼠标位置为中心进行缩放
     *
     * @param deltaY 滚轮增量
     * @param mouseX 鼠标相对于 Canvas 的 X 坐标
     */
    public void handleZoom(double deltaY, double mouseX) {
        // 1. 锁定当前鼠标指向的时间点（缩放中心）
        double mouseTime = getTimeByX(mouseX);

        // 2. 计算新的 dayWidth (使用更细腻的缩放率)
        double zoomSpeed = Math.min(Math.abs(deltaY) / 600.0, 0.2);
        double zoomFactor = (deltaY < 0) ? (1 + zoomSpeed) : (1 - zoomSpeed);
        double newDayWidth = dayWidth * zoomFactor;

        // 3. 限制范围
        if (newDayWidth < 0.3) newDayWidth = 0.3; // 极小缩放，甚至可以看十年
        if (newDayWidth > 2000.0) newDayWidth = 2000.0; // 极大放大，可以看小时

        if (newDayWidth == dayWidth) return;
        this.dayWidth = newDayWidth;


        // 4. 调整起始时间，保持鼠标指向的时间点在屏幕上的 X 坐标不动
        double pixelOffset = mouseX;
        double msVisible = (pixelOffset / dayWidth) * MS_PER_DAY;
        this.startTimeMillis = mouseTime - msVisible;

        // 5. 刷新
        reLayout();
        chart.redraw();
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
                startTimeMillis = (long) (startT + (deltaT * progress));

                reLayout();
                chart.redraw();
            }

            @Override
            protected void onComplete() {
                // 动画正常结束，清除引用
                currentAnimation = null;

                // 最终位置补偿
                scrollTop = startS + deltaS;
                startTimeMillis = (long) (startT + deltaT);
                reLayout();
                chart.redraw();
            }

            @Override
            protected void onCancel() {
                // 动画被取消时，也清除引用
                currentAnimation = null;
            }
        };

        // 启动新动画
        currentAnimation.run(400); // 稍微加快到 400ms 增加响应感
    }
}
