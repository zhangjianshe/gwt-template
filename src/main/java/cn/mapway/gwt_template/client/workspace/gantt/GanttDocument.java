package cn.mapway.gwt_template.client.workspace.gantt;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.workspace.events.GanttHitResult;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTaskResponse;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskResponse;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.user.client.rpc.AsyncCallback;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLImageElement;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GanttDocument {
    //甘特图的头部高度
    public static final double GANTT_HEAD_HEIGHT = 64;
    public static final String COLOR_BORDER_HOVER = "skyblue";
    public static final String COLOR_BORDER = "#e0e0e0";
    private final long MS_PER_DAY = 24 * 60 * 60 * 1000L;
    @Getter
    private final List<GanttItem> flatItems;
    private final List<GanttItem> rootItems;
    @Getter
    private final Map<String, GanttItem> items;
    @Getter
    private final double dayWidth = 40.0; // 默认一天 40 像素
    @Setter
    GanttChart chart;
    double scrollTop = 0;
    double totalHeight = 0;
    @Setter
    boolean isDraggingLeftPanel = false;
    List<GanttItem> selectedItems = new ArrayList<>();
    Map<String, HTMLImageElement> avatars = new HashMap<>();
    @Getter
    private List<DevProjectTaskEntity> rootTasks;
    private long startTimeMillis = System.currentTimeMillis();   // 视图起始时间戳
    private double leftPanelSize = 400;

    public GanttDocument() {
        rootTasks = new ArrayList<DevProjectTaskEntity>();
        flatItems = new ArrayList<>();
        rootItems = new ArrayList<>();
        items = new HashMap<>();
    }

    public void populateCharge(GanttItem item) {
        String url = AppResource.INSTANCE.noData().getSafeUri().asString();
        if (!StringUtil.isBlank(item.getEntity().getChargeAvatar())) {
            url = item.getEntity().getChargeAvatar();
        }
        if (StringUtil.isBlank(url)) {
            url = AppResource.INSTANCE.emptyAvatar().getSafeUri().asString();
        }

        // 简单的缓存判断
        if (item.getAvatar() != null && url.equals(item.getAvatar().src)) {
            return;
        }
        HTMLImageElement img = avatars.get(url);
        if (img == null) {
            img = (HTMLImageElement) DomGlobal.document.createElement("img");
            avatars.put(url, img);
            img.src = url;
            img.onload = (e) -> {
                chart.redraw();
                return null;
            };
        }
        item.setAvatar(img);

    }

    // 将日期转换为相对于时间轴起始点的像素偏移
    public double getXByDate(Long dateMillis) {
        if (dateMillis == null) return 0;
        double diffDays = (dateMillis - startTimeMillis) / (24.0 * 60 * 60 * 1000L);
        return diffDays * dayWidth;
    }

    // 将 Canvas 上的 X 坐标转换为日期时间戳
    public long getTimeByX(double x) {
        double days = x / dayWidth;
        return (long) (startTimeMillis + (days * 24.0 * 60 * 60 * 1000L));
    }

    public long getTimeBySpan(double span) {
        double days = span / dayWidth;
        return (long) ((days * 24.0 * 60 * 60 * 1000L));
    }

    public void loadDocument(String projectId) {
        QueryProjectTaskRequest request = new QueryProjectTaskRequest();
        request.setProjectId(projectId);
        AppProxy.get().queryProjectTask(request, new AsyncCallback<RpcResult<QueryProjectTaskResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryProjectTaskResponse> result) {
                if (result.isSuccess()) {
                    buildTree(result.getData());
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    /**
     * 构造完成后 rootItems 是根任务 items 是MAP
     *
     * @param data
     */
    private void buildTree(QueryProjectTaskResponse data) {
        rootTasks = data.getRootTasks();
        flatItems.clear();
        items.clear();

        recursiveBuild(null, rootTasks);
        // 重新计算起始时间：找到所有任务中的最早时间
        if (!flatItems.isEmpty()) {
            long minStart = Long.MAX_VALUE;
            for (GanttItem item : flatItems) {
                long t = item.getEntity().getStartTime().getTime();
                if (t < minStart) minStart = t;
            }
            // 将视图起始时间设置为最早任务的前两天，留点呼吸空间
            startTimeMillis = minStart - (long) ((getLeftPanelWidth() / getDayWidth() + 2) * MS_PER_DAY);
        } else {
            startTimeMillis = System.currentTimeMillis() - (long) (getLeftPanelWidth() / getDayWidth() + 2) * MS_PER_DAY;
        }
        reLayout();
    }

    private void recursiveBuild(GanttItem parent, List<DevProjectTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        for (DevProjectTaskEntity task : tasks) {
            GanttItem item = new GanttItem();
            item.setEntity(task);
            if (item.getEntity().getStartTime().getTime() < startTimeMillis) {
                startTimeMillis = item.getEntity().getStartTime().getTime();
            }
            if (parent != null) {
                parent.addChild(item);
            } else {
                item.setParent(null);
                rootItems.add(item);
            }
            items.put(task.getId(), item);
            flatItems.add(item);
            populateCharge(item);
            recursiveBuild(item, task.getChildren());
        }
    }

    /**
     * 重新布局所有的任务
     */
    public void reLayout() {
        totalHeight = 0;
        double top = GANTT_HEAD_HEIGHT;
        double left = 0;
        for (GanttItem item : rootItems) {
            top += layoutItem(item, top, left);
        }
        totalHeight = top - GANTT_HEAD_HEIGHT;
        chart.redraw();
    }

    private double layoutItem(GanttItem item, double top, double left) {
        double h = item.getDesiredHeight();

        // rect 存储在屏幕上的显示位置
        item.getRect().set(left, top, chart.getOffsetWidth(), h);

        double totalHeight = h;
        for (GanttItem child : item.getChildren()) {
            // 子任务纵向累加，横向缩进
            top += totalHeight;
            totalHeight += layoutItem(child, top, left);

        }
        return totalHeight;
    }

    public void clear() {
        flatItems.clear();
        rootItems.clear();
        items.clear();
        chart.redraw();
    }

    public boolean isEmpty() {
        return rootItems.isEmpty();
    }

    public boolean hitTest(GanttHitResult result, Size logic) {

        if (logic.y >= 0 && logic.y < GANTT_HEAD_HEIGHT / 2.) {
            result.monthBar();
            return true;
        } else if (logic.y >= GANTT_HEAD_HEIGHT / 2 && logic.y < GANTT_HEAD_HEIGHT) {
            result.dayBar();
            return true;
        }

        if (logic.x >= getLeftPanelWidth() - 5 && logic.x <= getLeftPanelWidth() + 5) {
            result.hitTestResizeLeftPanel();
            return true;
        }

        if (logic.x > getLeftPanelWidth() && logic.y > GANTT_HEAD_HEIGHT + scrollTop + totalHeight) {
            //右侧空白区
            result.hitTestGanttEmpty();
            return true;
        }

        for (GanttItem item : flatItems) {
            boolean b = item.hitTest(this, result, logic);
            if (b) return true;
        }

        result.reset();
        return false;
    }

    public double getLeftPanelWidth() {
        return leftPanelSize;
    }

    public void offsetTimeline(double deltaX, double deltaY) {
        // 将位移像素转为毫秒
        long deltaMillis = getTimeBySpan(deltaX);
        // 左右平移视图起始时间
        startTimeMillis -= deltaMillis;
        chart.redraw();
    }

    /**
     * 核心：计算当前起始时间距离“对齐日期”的像素偏移
     */
    public double getPixelOffset() {
        long aligned = getAlignedStartTime();
        // 计算当前显示起点距离对齐起点的时间差，转为像素
        return ((double) (startTimeMillis - aligned) / MS_PER_DAY) * dayWidth;
    }

    /**
     * 获取对齐到当天 0 点的起始日期
     * 修正：考虑本地时区偏移，确保日期数字显示正确
     */
    public long getAlignedStartTime() {
        // 显式转为 double，确保 GWT 不会传入一个模拟的 long 对象
        double timestamp = (double) this.startTimeMillis;
        elemental2.core.JsDate date = new elemental2.core.JsDate(timestamp);
        date.setHours(0, 0, 0, 0);
        return (long) date.getTime();
    }

    public long getViewStartTime() {
        // 确保有一个合理的默认值
        if (startTimeMillis <= 0) {
            elemental2.core.JsDate now = new elemental2.core.JsDate();
            now.setHours(0, 0, 0, 0);
            startTimeMillis = (long) now.getTime();
        }
        return startTimeMillis;
    }

    public void offsetLeftPanel(double deltaX, double deltaY) {
        leftPanelSize += deltaX;
        if (leftPanelSize < 100) {
            leftPanelSize = 100;
        }
        chart.redraw();
    }

    public String getFixBorderColor() {

        if (isDraggingLeftPanel) {
            return GanttDocument.COLOR_BORDER_HOVER;
        } else {
            return GanttDocument.COLOR_BORDER;
        }
    }

    public void updateEntityTime(GanttItem ganttItem, double oldStart, double oldEstimate) {
        DevProjectTaskEntity oldEntity = ganttItem.getEntity();
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
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateProjectTaskResponse> result) {
                if (result.isSuccess()) {

                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });

    }


    public void clearSelection() {
        for (GanttItem item : selectedItems) {
            item.setSelected(false);
        }
        selectedItems.clear();
    }

    public void selectPrev() {
        if (flatItems.isEmpty()) {
            return;
        }
        if (selectedItems.isEmpty()) {
            // 如果没有选中项，默认选中第一项
            appendSelect(flatItems.get(0), true);
        } else {
            GanttItem currentSelect = selectedItems.get(0);
            int currentIndex = flatItems.indexOf(currentSelect);

            // 如果当前不是第一项，则选中前一项
            if (currentIndex > 0) {
                appendSelect(flatItems.get(currentIndex - 1), true);
            }
        }
    }

    public void selectNext() {
        if (flatItems.isEmpty()) {
            return;
        }
        if (selectedItems.isEmpty()) {
            // 如果没有选中项，默认选中第一项
            appendSelect(flatItems.get(0), true);
        } else {
            GanttItem currentSelect = selectedItems.get(0);
            int currentIndex = flatItems.indexOf(currentSelect);

            // 如果当前不是最后一项，则选中后一项
            if (currentIndex < flatItems.size() - 1) {
                appendSelect(flatItems.get(currentIndex + 1), true);
            }
        }
    }

    /**
     * 封装选择方法，确保状态同步
     *
     * @param item        要选中的项
     * @param clearOthers 是否清除之前的选择
     */
    public void appendSelect(GanttItem item, boolean clearOthers) {
        if (clearOthers) {
            clearSelection();
        }
        if (item != null) {
            item.setSelected(true);
            if (!selectedItems.contains(item)) {
                selectedItems.add(item);
            }
        }
    }
}
