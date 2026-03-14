package cn.mapway.gwt_template.client.workspace.gantt;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
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
    @Getter
    private final List<GanttItem> rootItems;
    @Getter
    private final Map<String, GanttItem> items;
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
    DropLocation lastDropLocation = new DropLocation();//拖动任务排序时记录当前的位置
    @Getter
    private double dayWidth = 40.0; // 默认一天 40 像素
    @Getter
    private List<DevProjectTaskEntity> rootTasks;
    private double startTimeMillis = (double) System.currentTimeMillis();   // 视图起始时间戳
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

    // 将日期转换为相对于左侧面板边缘的像素偏移
    public double getXByDate(double dateMillis) {
        return (dateMillis - startTimeMillis) / (double) MS_PER_DAY * dayWidth;
    }

    // 对应的逆运算：将屏幕 X 坐标转回时间戳
    public double getTimeByX(double x) {
        double days = (x - leftPanelSize) / dayWidth;
        return startTimeMillis + (days * (double) MS_PER_DAY);
    }

    // 获取不带左侧偏移的纯跨度像素转时间
    public double getTimeBySpan(double span) {
        return (span / dayWidth) * (double) MS_PER_DAY;
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
        // 1. 在构建树之前，先对每一层级的数据进行 rank 排序
        sortEntitiesByRank(rootTasks);

        recursiveBuild(null, rootTasks);
        // 关键：根据刚才 recursiveBuild 设置的 expanded 状态，重新生成可见任务列表
        rebuildFlatItems();
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
            // 使用数据库中的字段初始化展开状态，如果字段为 null 则默认为 true
            boolean shouldExpand = task.getInitExpand() == null || task.getInitExpand();
            item.setExpanded(shouldExpand);
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
            // 注意：这里不要直接 add 到 flatItems，
            // 因为如果父节点是折叠的，子节点不应该出现在扁平列表中。
            // 我们统一在 buildTree 的最后通过 rebuildFlatItems 处理。
            //flatItems.add(item);
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
            item.setLevel(0); // 根节点级别为 0
            top += layoutItem(item, top, left);
        }
        totalHeight = top - GANTT_HEAD_HEIGHT;
    }

    private double layoutItem(GanttItem item, double top, double left) {
        double h = item.getDesiredHeight();

        // 根据当前 level 自动计算缩进（如果你在绘图时使用了 level）
        // item.setIndent(item.getLevel() * 16);

        item.getRect().set(left, top - scrollTop, chart.getOffsetWidth(), h);

        double th = h;
        if (item.isExpanded()) {
            for (GanttItem child : item.getChildren()) {
                // 核心修正：子节点的 level 永远等于父节点 level + 1
                child.setLevel(item.getLevel() + 1);
                th += layoutItem(child, top + th, left);
            }
        } else {
            resetChildrenRect(item);
        }
        return th;
    }

    private void resetChildrenRect(GanttItem parent) {
        for (GanttItem child : parent.getChildren()) {
            child.getRect().set(0, -1000, 0, 0); // 将隐藏节点移出屏幕
            resetChildrenRect(child);
        }
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

    private void sortEntitiesByRank(List<DevProjectTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) return;

        // 按 rank 升序排列
        tasks.sort((a, b) -> {
            Double r1 = a.getRank() == null ? 0.0 : a.getRank();
            Double r2 = b.getRank() == null ? 0.0 : b.getRank();
            return Double.compare(r1, r2);
        });

        // 递归排序子项
        for (DevProjectTaskEntity task : tasks) {
            sortEntitiesByRank(task.getChildren());
        }
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
        // 1. 处理垂直滚动
       /* this.scrollTop += deltaY;

        // 边界检查：不要滚出任务列表底部
        double maxScroll = Math.max(0, totalHeight - (chart.getOffsetHeight() - GANTT_HEAD_HEIGHT));
        if (this.scrollTop < 0) this.scrollTop = 0;
        if (this.scrollTop > maxScroll) this.scrollTop = maxScroll;
*/
        // 2. 处理水平滚动 (时间轴平移)
        // 关键：deltaX 是像素，通过 double 运算转为时间偏移
        double msPerPixel = (double) MS_PER_DAY / dayWidth;
        double deltaMillis = deltaX * msPerPixel;

        // 更新起始时间 (double 类型)
        this.startTimeMillis -= deltaMillis;

        // 3. 触发重绘
        reLayout(); // 重新计算所有 Item 的 Y 坐标（受 scrollTop 影响）
        chart.redraw();
    }

    /**
     * 获取对齐到当天 0 点的起始日期
     * 修正：考虑本地时区偏移，确保日期数字显示正确
     */
    public long getAlignedStartTime() {
        // 显式转为 double，确保 GWT 不会传入一个模拟的 long 对象
        double timestamp = this.startTimeMillis;
        elemental2.core.JsDate date = new elemental2.core.JsDate(timestamp);
        date.setHours(0, 0, 0, 0);
        return (long) date.getTime();
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
                parentItem.setExpanded(true);
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

        double targetStartTime = item.getEntity().getStartTime().getTime()
                - 2 * MS_PER_DAY
                - getTimeBySpan(getLeftPanelWidth());

        scrollToTimestamp(targetStartTime, targetScrollTop);
    }

    public void scrollToNow() {
        double targetStartTime = System.currentTimeMillis()
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
        populateCharge(item);
    }

    private void copyData(DevProjectTaskEntity updatedTask, DevProjectTaskEntity entity) {
        entity.setCharger(updatedTask.getCharger());
        entity.setName(updatedTask.getName());
        entity.setChargeUserName(updatedTask.getChargeUserName());
        entity.setChargeAvatar(updatedTask.getChargeAvatar());
        entity.setKind(updatedTask.getKind());
        entity.setSummary(updatedTask.getSummary());
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

    // 在 GanttDocument.java 中
    public void toggleExpand(GanttItem item) {
        if (item == null || item.getChildren().isEmpty()) {
            return;
        }

        // 切换状态
        item.setExpanded(!item.isExpanded());
        item.getEntity().setInitExpand(item.isExpanded());

        // 关键：重新构建扁平列表
        // 只有展开的任务及其子任务才会进入 flatItems
        rebuildFlatItems();

        // 重新计算坐标（Y轴会发生变化）
        reLayout();

        // sync to database
        syncExpandToDb(item.getEntity());
    }

    private void syncExpandToDb(DevProjectTaskEntity entity) {
        DevProjectTaskEntity temp = new DevProjectTaskEntity();
        temp.setId(entity.getId());
        temp.setProjectId(entity.getProjectId());
        temp.setInitExpand(entity.getInitExpand());
        UpdateProjectTaskRequest request = new UpdateProjectTaskRequest();
        request.setProjectTask(temp);
        AppProxy.get().updateProjectTask(request, new AsyncAdaptor<RpcResult<UpdateProjectTaskResponse>>() {
            @Override
            public void onData(RpcResult<UpdateProjectTaskResponse> result) {
                //不重要的更新 忽略
            }
        });
    }

    // 在 GanttDocument.java 中
    public void rebuildFlatItems() {
        flatItems.clear();
        for (GanttItem root : rootItems) {
            collectVisibleItems(root);
        }
    }

    private void collectVisibleItems(GanttItem item) {
        flatItems.add(item);

        // 如果当前节点已展开，则继续递归收集子节点
        if (item.isExpanded()) {
            for (GanttItem child : item.getChildren()) {
                collectVisibleItems(child);
            }
        }
    }

    // 辅助方法
    private boolean isDescendant(GanttItem draggedItem, String potentialParentId) {
        if (potentialParentId == null || potentialParentId.isEmpty()) return false;
        GanttItem current = items.get(potentialParentId);
        while (current != null) {
            if (current == draggedItem) return true;
            current = current.getParent();
        }
        return false;
    }

    public void reorderItem(String taskId, String newParentId, double newRank) {
        GanttItem item = items.get(taskId);
        if (item == null) return;

        // 在 reorderItem 中
        if (isDescendant(item, newParentId)) {
            ClientContext.get().toast(0, 0, "不能将任务移动到自己的子任务下");
            return;
        }

        // 先记录旧状态，用于失败回滚（或者简单处理直接 reload）
        // 先更改本地，提供即时反馈 (Optimistic UI)
        applyLocalChange(item, newParentId, newRank);

        UpdateProjectTaskRequest request = new UpdateProjectTaskRequest();
        DevProjectTaskEntity task = new DevProjectTaskEntity();

        task.setId(taskId);
        task.setRank(newRank);
        task.setParentId(newParentId);
        // 关键修正：projectId 必须是任务所属的项目 ID，不能是任务自己的 ID
        task.setProjectId(item.getEntity().getProjectId());

        request.setProjectTask(task);
        AppProxy.get().updateProjectTask(request, new AsyncCallback<RpcResult<UpdateProjectTaskResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, "保存排序失败: " + caught.getMessage());
                reload(); // 失败后通过重新加载来回滚本地更改
            }

            @Override
            public void onSuccess(RpcResult<UpdateProjectTaskResponse> result) {
                if (!result.isSuccess()) {
                    ClientContext.get().toast(0, 0, "保存排序失败: " + result.getMessage());
                    reload();
                }
                // 成功时不需要操作，因为 applyLocalChange 已经处理了 UI
            }
        });
    }

    private void applyLocalChange(GanttItem item, String newParentId, double newRank) {
        // 1. 更新实体数据
        item.getEntity().setRank(newRank);
        item.getEntity().setParentId(newParentId);

        // 2. 解除旧的父子关系
        if (item.getParent() != null) {
            item.getParent().getChildren().remove(item);
        } else {
            getRootItems().remove(item);
        }

        // 3. 建立新的父子关系
        if (newParentId == null || newParentId.isEmpty()) {
            item.setParent(null);
            getRootItems().add(item);
        } else {
            GanttItem newParent = items.get(newParentId);
            if (newParent != null) {
                item.setParent(newParent);
                newParent.getChildren().add(item);
            }
        }

        // 4. 核心：重新执行排序、平整化、布局
        rebuildAndLayout();
        chart.redraw();
    }

    public void rebuildAndLayout() {
        // 1. 每一层级按新的 rank 排序
        sortItems(rootItems);

        // 2. 重新构建扁平列表（考虑展开/收缩状态）
        rebuildFlatItems();

        // 3. 重新计算所有 Item 的 y 轴坐标
        reLayout();
    }

    private void sortItems(List<GanttItem> itemsList) {
        if (itemsList == null || itemsList.isEmpty()) return;

        itemsList.sort((a, b) -> {
            Double r1 = a.getEntity().getRank() == null ? 0.0 : a.getEntity().getRank();
            Double r2 = b.getEntity().getRank() == null ? 0.0 : b.getEntity().getRank();
            return Double.compare(r1, r2);
        });

        for (GanttItem item : itemsList) {
            if (!item.getChildren().isEmpty()) {
                sortItems(item.getChildren());
            }
        }
    }

    public DevProjectTaskEntity getFirstSelected() {
        if (!isValid()) {
            return null;
        }
        if (selectedItems.isEmpty()) {
            return null;
        }
        return selectedItems.get(0).getEntity();

    }

    public void moveFirstSelectLevelDown() {
        if (selectedItems.isEmpty()) return;
        GanttItem item = selectedItems.get(0);

        // 1. 获取当前同级列表
        List<GanttItem> siblings = (item.getParent() == null) ? rootItems : item.getParent().getChildren();
        int index = siblings.indexOf(item);

        // 2. 只有不是第一个兄弟时才能缩进
        if (index > 0) {
            GanttItem prevSibling = siblings.get(index - 1);

            // 计算新的 rank：排在 prevSibling 的所有子任务之后
            double newRank = 1.0;
            if (!prevSibling.getChildren().isEmpty()) {
                GanttItem lastChild = prevSibling.getChildren().get(prevSibling.getChildren().size() - 1);
                newRank = lastChild.getEntity().getRank() + 1.0;
            }

            // 3. 执行持久化并更新 UI
            reorderItem(item.getEntity().getId(), prevSibling.getEntity().getId(), newRank);

            // 4. 自动展开父节点，确保能看到移动后的自己
            if (!prevSibling.isExpanded()) {
                toggleExpand(prevSibling);
            }
        } else {
            ClientContext.get().toast(0, 0, "已经是该层级的第一个任务，无法缩进");
        }
    }

    public void moveFirstSelectLevelUp() {
        if (selectedItems.isEmpty()) return;
        GanttItem item = selectedItems.get(0);
        GanttItem parent = item.getParent();

        // 1. 如果没有父节点，说明已经在最顶层
        if (parent == null) {
            ClientContext.get().toast(0, 0, "已经在最顶层");
            return;
        }

        // 2. 找到爷爷节点（如果爷爷为空，则提升到根列表）
        GanttItem grandParent = parent.getParent();
        String newParentId = (grandParent == null) ? null : grandParent.getEntity().getId();

        // 3. 计算新的 rank：排在原父节点之后
        // 假设原父节点 rank 是 10, 我们给它 10.1, 10.2...
        // 为了简单，我们取 parent 之后那个兄弟的中点，或者直接 +1
        List<GanttItem> grandSiblings = (grandParent == null) ? rootItems : grandParent.getChildren();
        int parentIndex = grandSiblings.indexOf(parent);

        double newRank;
        if (parentIndex < grandSiblings.size() - 1) {
            // 取中间值
            newRank = (parent.getEntity().getRank() + grandSiblings.get(parentIndex + 1).getEntity().getRank()) / 2.0;
        } else {
            newRank = parent.getEntity().getRank() + 1.0;
        }

        // 4. 执行持久化并更新 UI
        reorderItem(item.getEntity().getId(), newParentId, newRank);
    }

    public void toggleFirstSelect() {
        if (selectedItems.isEmpty()) return;
        if (selectedItems.get(0).getChildren().isEmpty()) {
            return;
        }
        GanttItem item = selectedItems.get(0);
        toggleExpand(item);
    }

    public void shrinkFirstSelect() {
        if (selectedItems.isEmpty()) return;
        if (selectedItems.get(0).getChildren().isEmpty()) {
            return;
        }
        GanttItem item = selectedItems.get(0);
        if (item.isExpanded()) {
            item.setExpanded(false);
            rebuildFlatItems();
            reLayout();
        }

    }

    public void expandFirstSelect() {

        if (selectedItems.isEmpty()) return;
        if (selectedItems.get(0).getChildren().isEmpty()) {
            return;
        }
        GanttItem item = selectedItems.get(0);
        if (!item.isExpanded()) {
            item.setExpanded(true);
            rebuildFlatItems();
            reLayout();
        }
    }

    public GanttItem getFirstSelectItem() {
        if (selectedItems.isEmpty()) return null;
        return selectedItems.get(0);
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
        double pixelOffset = mouseX - leftPanelSize;
        double msVisible = (pixelOffset / dayWidth) * (double) MS_PER_DAY;
        this.startTimeMillis = mouseTime - msVisible;

        // 5. 刷新
        reLayout();
        chart.redraw();
    }

    public List<GanttItem> getSiblings(GanttItem ganttItem) {
        if (ganttItem == null) return new ArrayList<>();

        // 直接返回内存中的引用，无需计算
        List<GanttItem> list = (ganttItem.getParent() == null)
                ? rootItems
                : ganttItem.getParent().getChildren();

        // 确保这个 list 是有序的（你的 recursiveBuild 和 sortItems 应该已经保证了这一点）
        return list;
    }
}
