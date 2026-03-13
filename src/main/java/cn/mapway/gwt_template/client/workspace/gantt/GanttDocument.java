package cn.mapway.gwt_template.client.workspace.gantt;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.workspace.events.GanttHitResult;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.*;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.rpc.AsyncCallback;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLImageElement;
import elemental2.promise.IThenable;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

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
    @Getter
    @Setter
    double scrollTop = 0;
    @Getter
    double totalHeight = 0;
    @Setter
    boolean isDraggingLeftPanel = false;
    List<GanttItem> selectedItems = new ArrayList<>();
    Map<String, HTMLImageElement> avatars = new HashMap<>();
    int maxCode = -1;
    int maxCodeLength = 3;
    @Getter
    String projectId;
    @Getter
    boolean valid = false;
    String errorMessage = "没有有用的消息";
    @Getter
    private List<DevProjectTaskEntity> rootTasks;
    private long startTimeMillis = System.currentTimeMillis();   // 视图起始时间戳
    private double leftPanelSize = 400;
    private Animation currentAnimation = null;

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
        this.projectId = projectId;
        flatItems.clear();
        rootItems.clear();
        items.clear();
        rootTasks.clear();
        chart.redraw();

        if (StringUtil.isBlank(projectId)) {
            valid = false;
            errorMessage = "没有项目ID";
            return;
        }

        QueryProjectTaskRequest request = new QueryProjectTaskRequest();
        request.setProjectId(projectId);
        AppProxy.get().queryProjectTask(request, new AsyncCallback<RpcResult<QueryProjectTaskResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
                valid = false;
                errorMessage = caught.getMessage();
            }

            @Override
            public void onSuccess(RpcResult<QueryProjectTaskResponse> result) {
                if (result.isSuccess()) {
                    valid = true;
                    errorMessage = "";
                    buildTree(result.getData());
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                    valid = false;
                    errorMessage = result.getMessage();
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
        maxCodeLength = 3;
        recursiveBuild(null, rootTasks);
        maxCodeLength = Math.max(maxCodeLength, (maxCode + "").length());
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
        chart.redraw();
    }

    private void recursiveBuild(GanttItem parent, List<DevProjectTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        for (DevProjectTaskEntity task : tasks) {
            GanttItem item = new GanttItem();
            item.setEntity(task);
            if (task.getCode() != null && task.getCode() > maxCode) {
                maxCode = task.getCode();
            }
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
    }

    private double layoutItem(GanttItem item, double top, double left) {
        double h = item.getDesiredHeight();

        // 关键修正：在计算 Rect 时减去 scrollTop
        // 这样绘制时 item.getRect().y 就会随着滚动而变化
        item.getRect().set(left, top - scrollTop, chart.getOffsetWidth(), h);

        double th = h;
        for (GanttItem child : item.getChildren()) {
            th += layoutItem(child, top + th, left);
        }
        return th;
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
        if (logic.x < getLeftPanelWidth() && logic.y > GANTT_HEAD_HEIGHT + scrollTop + totalHeight) {
            //左侧空白区
            result.hitTestGanttControlEmpty();
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
                    oldEntity.setStartTime(new Timestamp((long) oldStart));
                    oldEntity.setEstimateTime(new Timestamp((long) oldEstimate));
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
        if (selectedItems.size() == 1) {
            scrollToItem(selectedItems.get(0));
        }
        chart.fireEvent(CommonEvent.selectEvent(item.getEntity()));
    }

    public String formatTaskCode(Integer code) {
        return "#" + StringUtil.formatNumber(code, maxCodeLength);
    }

    /**
     * 新插入一个任务
     *
     * @param entity
     */
    public void insertTask(DevProjectTaskEntity entity) {
        // 1. 设置合理的默认时间 (如果实体内没有)
        if (entity.getStartTime() == null) {
            entity.setStartTime(new Timestamp(System.currentTimeMillis()));
        }
        if (entity.getEstimateTime() == null) {
            entity.setEstimateTime(new Timestamp(entity.getStartTime().getTime() + MS_PER_DAY * 3));
        }

        // 2. 找到挂载点
        GanttItem newItem = new GanttItem();
        newItem.setEntity(entity);

        if (StringUtil.isBlank(entity.getParentId())) {
            rootTasks.add(entity);
            rootItems.add(newItem);
            newItem.setLevel(0);
        } else {
            GanttItem parentItem = items.get(entity.getParentId());
            if (parentItem != null) {
                parentItem.addChild(newItem);
            }
        }

        // 3. 注册到全局索引
        items.put(entity.getId(), newItem);
        populateCharge(newItem); // 记得拉取头像

        // 4. 更新 flatItems (最简单的方式是增量添加或局部重排，这里暂时沿用重排逻辑但保留状态)
        rebuildFlatList();
        reLayout();
        chart.redraw();
    }

    private void rebuildFlatList() {
        flatItems.clear();
        for (GanttItem root : rootItems) {
            traverse(root);
        }
    }

    private void traverse(GanttItem item) {
        flatItems.add(item);
        for (GanttItem child : item.getChildren()) {
            traverse(child);
        }
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

    public void scrollToItem(GanttItem item) {
        double targetScrollTop = scrollTop;
        double itemTop = item.getRect().y;
        double itemBottom = itemTop + item.getRect().height;
        double viewTop = GANTT_HEAD_HEIGHT;
        double viewBottom = chart.getOffsetHeight();

        if (itemTop < viewTop) {
            targetScrollTop = scrollTop - (viewTop - itemTop);
        } else if (itemBottom > viewBottom) {
            targetScrollTop = scrollTop + (itemBottom - viewBottom);
        }

        long targetStartTime = item.getEntity().getStartTime().getTime()
                - 2 * MS_PER_DAY
                - getTimeBySpan(getLeftPanelWidth());

        scrollToTimestamp(targetStartTime, targetScrollTop);
    }

    public void scrollToNow() {
        long targetStartTime = System.currentTimeMillis()
                - getTimeBySpan((chart.getOffsetWidth() - getLeftPanelWidth()) / 2.)
                - getTimeBySpan(getLeftPanelWidth());

        scrollToTimestamp(targetStartTime, scrollTop);
    }

    public void updateEntity(DevProjectTaskEntity taskEntity) {
        if (taskEntity == null) {
            return;
        }
        GanttItem item = items.get(taskEntity.getId());
        if (item == null) {
            return;
        }
        copyData(taskEntity, item.getEntity());
        item.setEntity(item.getEntity());
        chart.redraw();
    }

    private void copyData(DevProjectTaskEntity updatedTask, DevProjectTaskEntity entity) {
        entity.setCharger(updatedTask.getCharger());
        entity.setName(updatedTask.getName());
        entity.setChargeUserName(updatedTask.getChargeUserName());
        entity.setChargeAvatar(updatedTask.getChargeAvatar());
        entity.setKind(updatedTask.getKind());
    }

    public void reload() {
        loadDocument(projectId);
    }

    public String getMessage() {
        return errorMessage;
    }

    public void deleteItem(GanttItem ganttItem) {
        if (ganttItem == null) {
            return;
        }
        if (!ganttItem.getEntity().getChildren().isEmpty()) {
            ClientContext.get().confirm("不能删除有子任务的任务");
            return;
        }
        String msg = "删除任务" + ganttItem.getEntity().getName() + "?";
        ClientContext.get().confirmDelete(msg).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
            @Override
            public @Nullable IThenable<Object> onInvoke(Void p0) {
                doDeleteItem(ganttItem);
                return null;
            }
        });

    }

    private void doDeleteItem(GanttItem ganttItem) {

        DeleteProjectTaskRequest request = new DeleteProjectTaskRequest();
        request.setTaskId(ganttItem.getEntity().getId());
        AppProxy.get().deleteProjectTask(request, new AsyncCallback<RpcResult<DeleteProjectTaskResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<DeleteProjectTaskResponse> result) {
                if (result.isSuccess()) {
                    removeItem(ganttItem);
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });

    }

    private void removeItem(GanttItem ganttItem) {
        if (ganttItem == null) {
            return;
        }
        int index = flatItems.indexOf(ganttItem);
        String id = ganttItem.getEntity().getId();

        // 1. 从父节点或根节点列表中移除自己
        if (ganttItem.getParent() == null) {
            rootItems.remove(ganttItem);
            // 同时同步 rootTasks 数据源，保持 Document 结构一致
            rootTasks.remove(ganttItem.getEntity());
        } else {
            ganttItem.getParent().getChildren().remove(ganttItem);
            // 如果 Entity 结构里也有 children 引用，也需要清理
            ganttItem.getParent().getEntity().getChildren().remove(ganttItem.getEntity());
        }

        // 2. 从全局索引和扁平列表中移除
        items.remove(id);
        rebuildFlatList();

        // 3. 处理选中状态：如果被删除的是当前选中项，需要清空选中
        selectedItems.remove(ganttItem);

        // 4. 关键：重新布局并重绘
        reLayout();

        // 自动选中下一项
        if (!flatItems.isEmpty()) {
            int nextIndex = Math.min(index, flatItems.size() - 1);
            appendSelect(flatItems.get(nextIndex), true);
        }

    }

}
