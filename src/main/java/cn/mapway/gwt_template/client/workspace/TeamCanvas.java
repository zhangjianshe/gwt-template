package cn.mapway.gwt_template.client.workspace;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DevProjectTeamEntity;
import cn.mapway.gwt_template.shared.db.DevProjectTeamMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectMemberRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectMemberResponse;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTeamRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTeamResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectMember;
import cn.mapway.gwt_template.shared.rpc.user.CommonPermission;
import cn.mapway.ui.client.IUserInfo;
import cn.mapway.ui.client.mvc.Rect;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.canvas.CanvasWidget;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import elemental2.core.JsArray;
import elemental2.dom.BaseRenderingContext2D;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLImageElement;
import elemental2.promise.IThenable;
import jsinterop.base.Js;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.mapway.gwt_template.client.workspace.TeamGroupNode.MEMBER_LINE_HEIGHT;
import static cn.mapway.gwt_template.client.workspace.TeamGroupNode.TITLE_HEIGHT;

/**
 * 团队架构画布 - 优化版
 * 功能：平移缩放、树状布局、左键选中、右键菜单、动态鼠标样式
 */
public class TeamCanvas extends CanvasWidget implements IData<List<DevProjectTeamEntity>> {

    private static final double PADDING_BOTTOM = 10.0;    // 节点底部的留白
    private static final double NODE_WIDTH = 180.0;      // 节点固定宽度

    private static final int LEVEL_GAP = 80;
    private static final int NODE_GAP = 20;


    private final List<TeamGroupNode> layoutNodes = new java.util.ArrayList<>();
    private final Map<String, TeamGroupNode> nodeMap = new HashMap<>();
    private final Size translation = new Size(0, 0); // 替代 translateX, translateY
    // 交互辅助
    private final Size lastMousePos = new Size(0, 0);   // 上一次鼠标位置 (屏幕坐标)
    private final Size startMousePos = new Size(0, 0);  // 按下时的鼠标位置 (屏幕坐标)
    private final Size dragOffset = new Size(0, 0);     // 鼠标相对于节点左上角的逻辑位移
    private final Rect contentBounds = new Rect();
    ActionMenu menuNode = new ActionMenu();
    private List<DevProjectTeamEntity> rootTeams;
    private List<TeamGroupNode> rootNodes = new ArrayList<>();
    CommonEventHandler menuNodeHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent event) {
            ActionMenu actionMenu = (ActionMenu) event.getSource();
            if (event.isSelect()) {
                ActionMenuKind kind = event.getValue();
                switch (kind) {
                    case AMK_ADD_GROUP:
                        onAddTeam(menuNode.getData());
                        break;
                    case AMK_ADD_MEMBER:
                        onAddMember(menuNode.getData().getData());
                        break;
                    case AMK_DELETE:
                        onDeleteTeam(menuNode.getData());
                        break;
                    default:
                }

            }
            actionMenu.hide();
        }
    };
    // 变换状态
    private double currentScale = 1.0;
    private TeamGroupNode draggedNode = null;
    private TeamGroupNode hoverDropTarget = null;          // 替代 hoverDropTargetId
    private boolean isDragging = false;
    private boolean isPanning = false; // 新增状态标识

    public TeamCanvas() {
        super();
        Style style = getElement().getStyle();
        style.setProperty("cursor", "grab");
        style.setProperty("userSelect", "none");
        style.setProperty("webkitUserSelect", "none");
        menuNode.addItem("➕ 添加子分组", ActionMenuKind.AMK_ADD_GROUP);
        menuNode.addItem("👤 添加人员", ActionMenuKind.AMK_ADD_MEMBER);
        menuNode.addItem("🗑️ 删除分组", ActionMenuKind.AMK_DELETE);

        menuNode.addCommonHandler(menuNodeHandler);
        menuNode.addCloseHandler(event -> {

            redraw();
        });

        // 1. 缩放逻辑
        addDomHandler(event -> {
            event.preventDefault();
            double mouseX = event.getRelativeX(getElement());
            double mouseY = event.getRelativeY(getElement());

            // 获取当前鼠标指向的逻辑位置
            Size logicBefore = toLogicPos(mouseX, mouseY);

            double delta = event.getDeltaY() > 0 ? 0.9 : 1.1;
            currentScale = Math.max(0.1, Math.min(currentScale * delta, 3.0));

            // 重新调整平移量，使 logicBefore 依然处于 mouseX, mouseY 位置
            translation.x = mouseX - logicBefore.x * currentScale;
            translation.y = mouseY - logicBefore.y * currentScale;

            redraw();
        }, MouseWheelEvent.getType());

        // 2. 右键菜单
        addDomHandler(event -> {
            event.preventDefault();
            event.stopPropagation();
        }, ContextMenuEvent.getType());

        // 3. 鼠标按下 (区分左右键)
        addMouseDownHandler(event -> {
            event.preventDefault();
            // 获取点击位置
            double rx = event.getRelativeX(getElement());
            double ry = event.getRelativeY(getElement());
            Size logicPos = toLogicPos(rx, ry);

            // 命中检测
            TeamHitResult hit = findHitTarget(logicPos.x, logicPos.y);

            // --- 处理右键点击 ---
            if (event.getNativeButton() == NativeEvent.BUTTON_RIGHT) {
                handleRightClick(hit, rx, ry);
                return;
            }

            // --- 处理左键点击 ---
            // 1. 全局清除选中状态（如果点击了空白或者新节点）
            for (TeamGroupNode n : layoutNodes) n.setSelected(false);

            switch (hit.area) {
                case EXPAND_BUTTON:
                    hit.node.setExpanded(!hit.node.isExpanded());
                    calculateLayout();
                    break;

                case MEMBER_ITEM:
                    hit.node.setSelected(true);
                    // 这里可以记录当前点击的成员，供后续 MouseUp 使用
                    DomGlobal.console.log("选中成员: " + hit.memberIndex);
                    break;

                case NODE_BODY:
                    hit.node.setSelected(true);
                    // 只有左键点击 Body 才会启动节点拖拽
                    startDraggingNode(hit.node, logicPos);
                    break;

                case NONE:
                    // 点击空白处，启动画布平移
                    startPanningCanvas(logicPos);
                    isPanning = true;
                    // 立即更新 lastMousePos，防止平移的第一帧发生坐标跳变
                    lastMousePos.set(rx, ry);
                    break;
            }
            redraw();
        });

        // 4. 鼠标移动 (处理平移和光标样式)
        addMouseMoveHandler(event -> {
            // 统一使用相对于元素的坐标
            double mx = event.getRelativeX(getElement());
            double my = event.getRelativeY(getElement());

            // 1. 性能守卫：微小移动不触发计算
            if (Math.abs(mx - lastMousePos.x) < 0.5 && Math.abs(my - lastMousePos.y) < 0.5) {
                return;
            }

            Size logicPos = toLogicPos(mx, my);

            if (draggedNode != null) {
                // 分支 A: 节点拖拽逻辑
                handleNodeDragging(mx, my, logicPos);
            } else if (isPanning) {
                // 分支 B: 画布平移逻辑
                handleCanvasPanning(mx, my);
            } else {
                // 分支 C: 静态悬停反馈
                handlePassiveHover(logicPos);
            }

            // 记录上一帧位置
            lastMousePos.set(mx, my);
        });

        // 5. 鼠标抬起
        addMouseUpHandler(event -> {
            // 停止 DOM 事件捕获
            com.google.gwt.user.client.DOM.releaseCapture(getElement());

            if (isDragging && draggedNode != null) {
                // 只有真正发生了移动才触发逻辑
                TeamGroupNode finalParent = findPotentialParent(draggedNode);

                // 逻辑判定：是否真的改变了父节点
                if (finalParent != null && !finalParent.data.getId().equals(draggedNode.data.getParentId())) {
                    if (!isInvalidDropTarget(finalParent)) {
                        doChangeTeamParent(draggedNode.data, finalParent.data);
                    } else {
                        // 非法操作，弹回原位
                        calculateLayout();
                    }
                } else {
                    // 原地放下或取消，恢复自动布局
                    calculateLayout();
                }
            }

            // 重置所有标志位
            resetInteractionState();
            setCursor("grab");
            redraw();
        });
    }

    private void setCursor(String cursorStyle) {
        // 只有当样式确实发生变化时才操作 DOM，减少性能损耗
        String current = getElement().getStyle().getProperty("cursor");
        if (!cursorStyle.equals(current)) {
            getElement().getStyle().setProperty("cursor", cursorStyle);
        }
    }

    private void resetInteractionState() {
        if (draggedNode != null) draggedNode.setBeingDragged(false);
        draggedNode = null;
        hoverDropTarget = null;
        isDragging = false;
        isPanning = false;
    }

    // 每次重建数据或 RPC 返回时同步
    private void syncNodes() {
        nodeMap.clear();
        for (TeamGroupNode node : layoutNodes) {
            nodeMap.put(node.data.getId(), node);
        }
    }

    private void handleNodeDragging(double mx, double my, Size logicPos) {
        // 5像素阈值检测：防止点击时的轻微抖动误触发拖拽
        if (!isDragging && startMousePos.distanceTo(mx, my) > 5) {
            isDragging = true;
            draggedNode.setBeingDragged(true);
        }

        if (isDragging) {
            // 更新节点物理坐标
            draggedNode.rect.x = logicPos.x - dragOffset.x;
            draggedNode.rect.y = logicPos.y - dragOffset.y;

            // 寻找潜在的父节点（用于改派部门）
            hoverDropTarget = findPotentialParent(draggedNode);

            // 更新光标样式
            if (hoverDropTarget != null && isInvalidDropTarget(hoverDropTarget)) {
                setCursor("not-allowed");
            } else {
                setCursor("grabbing");
            }
            redraw();
        }
    }

    private void handleCanvasPanning(double mx, double my) {
        translation.x += (mx - lastMousePos.x);
        translation.y += (my - lastMousePos.y);
        setCursor("grabbing");
        redraw();
    }


    private Size toLogicPos(double relativeX, double relativeY) {
        return new Size(
                (relativeX - translation.x) / currentScale,
                (relativeY - translation.y) / currentScale
        );
    }

    private void handleRightClick(TeamHitResult hit, double sx, double sy) {
        // 即使是右键，也建议把命中的节点设为选中态，方便用户知道在操作谁
        if (hit.node != null) {
            for (TeamGroupNode n : layoutNodes) n.setSelected(false);
            hit.node.setSelected(true);
            redraw();
        }

        switch (hit.area) {
            case MEMBER_ITEM:
                // 弹出成员管理菜单：修改角色、移除成员
                showTeamActionMenu(hit.node, hit.memberIndex, sx, sy);
                break;
            case NODE_BODY:
                // 弹出节点管理菜单：添加子部门、编辑名称、删除部门
                //showNodeContextMenu(hit.node, sx, sy);
                break;
            case NONE:
                // 弹出全局菜单：新建根部门、复位视图(Zoom to Fit)
                //showGlobalContextMenu(sx, sy);
                break;
        }
    }

    private TeamHitResult findHitTarget(double lx, double ly) {
        // 逆序遍历，确保点击的是最上层的节点（Z-index 优先）
        for (int i = layoutNodes.size() - 1; i >= 0; i--) {
            TeamHitResult result = layoutNodes.get(i).hitTest(lx, ly);
            if (result != null) {
                return result; // 一旦命中，立即返回
            }
        }

        // 如果没有任何节点被命中
        TeamHitResult none = new TeamHitResult();
        none.area = TeamHitTest.NONE;
        return none;
    }

    private void doChangeTeamParent(DevProjectTeamEntity child, DevProjectTeamEntity newParent) {
        // 1. 构造请求

        final String oldParentId = child.getParentId();
        UpdateProjectTeamRequest request = new UpdateProjectTeamRequest();
        child.setParentId(newParent.getId()); // 修改父 ID
        request.setProjectTeam(child);


        // 2. 调用 RPC
        AppProxy.get().updateProjectTeam(request, new AsyncCallback<RpcResult<UpdateProjectTeamResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, "移动失败: " + caught.getMessage());
                child.setParentId(oldParentId);
                calculateLayout();
                redraw(); // 失败回弹
            }

            @Override
            public void onSuccess(RpcResult<UpdateProjectTeamResponse> result) {
                if (result.isSuccess()) {
                    // 从原位置移除并加入新位置
                    moveNodeInLocalTree(child.getId(), newParent.getId());
                    calculateLayout();
                    redraw();
                    ClientContext.get().toast(0, 0, "层级已更新");
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                    child.setParentId(oldParentId);
                    calculateLayout();
                    redraw();
                }
            }
        });
    }

    private void moveNodeInLocalTree(String nodeId, String newParentId) {
        // 1. 同步原始数据实体 (Entity)
        DevProjectTeamEntity movedEntity = findAndRemoveFromTree(rootTeams, nodeId);
        if (movedEntity != null) {
            DevProjectTeamEntity parentEntity = findEntityInTree(rootTeams, newParentId);
            if (parentEntity != null) {
                if (parentEntity.getChildren() == null) {
                    parentEntity.setChildren(new java.util.ArrayList<>());
                }
                parentEntity.getChildren().add(movedEntity);
            }
        }

        // 2. 关键补丁：同步 LayoutNode 包装器引用
        // 如果不更新这个，calculateLayout 内部的递归可能还是走的老路
        TeamGroupNode childNode = findLayoutNodeById(nodeId);
        TeamGroupNode newParentNode = findLayoutNodeById(newParentId);

        if (childNode != null && newParentNode != null) {
            // 解除旧关系
            if (childNode.parent != null && childNode.parent.children != null) {
                childNode.parent.children.remove(childNode);
            }
            // 建立新关系
            childNode.parent = newParentNode;
            if (newParentNode.children == null) {
                newParentNode.children = new java.util.ArrayList<>();
            }
            newParentNode.children.add(childNode);
        }

        // 3. 执行重排与重绘
        calculateLayout();
        redraw();
    }

    private TeamGroupNode findLayoutNodeById(String nodeId) {
        return nodeMap.get(nodeId); // 瞬间定位，不再需要 for 循环
    }

    /**
     * 在树形结构中递归查找指定 ID 的实体对象
     *
     * @param teams    根节点列表或当前层级的节点列表
     * @param targetId 目标 ID
     * @return 找到的实体对象，未找到则返回 null
     */
    private DevProjectTeamEntity findEntityInTree(List<DevProjectTeamEntity> teams, String targetId) {
        if (teams == null || teams.isEmpty() || targetId == null) {
            return null;
        }

        for (DevProjectTeamEntity team : teams) {
            // 1. 检查当前节点
            if (targetId.equals(team.getId())) {
                return team;
            }

            // 2. 递归查找子节点
            if (team.getChildren() != null && !team.getChildren().isEmpty()) {
                DevProjectTeamEntity found = findEntityInTree(team.getChildren(), targetId);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void startPanningCanvas(Size locPosition) {
        this.isPanning = true;
        this.lastMousePos.copyFrom(locPosition);
        setCursor("grabbing");
        com.google.gwt.user.client.DOM.setCapture(getElement());
    }

    private void startDraggingNode(TeamGroupNode node, Size logicMousePos) {
        this.draggedNode = node;
        node.setBeingDragged(true);
        node.setSelected(true);
        clearAllSelectionsExcept(node);

        // 计算鼠标相对于节点左上角的逻辑偏移
        this.dragOffset.set(
                logicMousePos.x - node.rect.x,
                logicMousePos.y - node.rect.y
        );

        // 捕获交互并更新样式
        setCursor("grabbing");
        com.google.gwt.user.client.DOM.setCapture(getElement());

        // 记录拖拽开始时的位置，用于 5px 阈值判定
        // 注意：这里用屏幕坐标或相对坐标都可以，只要 MouseMove 判定时一致即可
        this.startMousePos.set(lastMousePos.x, lastMousePos.y);

        redraw();
    }

    /**
     * 清除除指定节点外所有节点的选中状态
     *
     * @param exceptNode 需要保持选中状态的节点，如果为 null 则清除所有
     */
    private void clearAllSelectionsExcept(@Nullable TeamGroupNode exceptNode) {
        for (TeamGroupNode node : layoutNodes) {
            if (node != exceptNode) {
                node.isSelected = false;
            }
        }
    }

    private void handlePassiveHover(Size lp) {
        // 2. 获取命中结果
        TeamHitResult hit = findHitTarget(lp.x, lp.y);

        // 3. 极其重要：清除所有节点的临时悬停状态，否则状态会“粘死”
        for (TeamGroupNode n : layoutNodes) {
            n.setHoveringMemberIndex(-1);
            n.setHoveringExpandBtn(false);
        }

        // 4. 更新状态
        if (hit.area == TeamHitTest.NONE) {
            setCursor("grab");
        } else {
            if (hit.area == TeamHitTest.EXPAND_BUTTON) {
                hit.node.setHoveringExpandBtn(true); // 按钮高亮触发点
                setCursor("pointer");
            } else if (hit.area == TeamHitTest.MEMBER_ITEM) {
                hit.node.setHoveringMemberIndex(hit.memberIndex); // 成员背景高亮触发点
                setCursor("pointer");
            } else {
                setCursor("default");
            }
        }

        // 5. 必须重绘，否则视觉上看不到状态变化
        redraw();
    }


    private void showTeamActionMenu(TeamGroupNode layoutNode, Integer memberIndex, double x, double y) {
        // 计算绝对位置弹出
        menuNode.setPopupPosition((int) x + getAbsoluteLeft(), (int) y + getAbsoluteTop());
        menuNode.setData(layoutNode);
        menuNode.show();
    }

    private void onAddTeam(TeamGroupNode layoutNode) {
        String title = "在" + layoutNode.getData().getName() + "下新建小组";
        ClientContext.get().input(title, "小组名称", "有特别含义的小组", "", new Callback() {
            @Override
            public void onFailure(Object reason) {

            }

            @Override
            public void onSuccess(Object result) {
                String teamName = (String) result;
                doAddTeam(layoutNode, teamName);
            }
        });
    }

    private void doAddTeam(TeamGroupNode parentNode, String teamName) {
        if (StringUtil.isBlank(teamName)) {
            return;
        }

        DevProjectTeamEntity parent = parentNode.getData();
        DevProjectTeamEntity newChild = new DevProjectTeamEntity();
        newChild.setName(teamName);
        newChild.setParentId(parent.getId());
        newChild.setProjectId(parent.getProjectId());
        newChild.setTeamPermission(CommonPermission.fromPermission(0).getPermission());
        newChild.setColor("");

        UpdateProjectTeamRequest request = new UpdateProjectTeamRequest();
        request.setProjectTeam(newChild);
        AppProxy.get().updateProjectTeam(request, new AsyncCallback<RpcResult<UpdateProjectTeamResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateProjectTeamResponse> result) {
                if (result.isSuccess()) {
                    DevProjectTeamEntity newEntity = result.getData().getProjectTeam();

                    // 1. 创建新的包装器并初始化状态
                    TeamGroupNode newNode = new TeamGroupNode();
                    newNode.setData(newEntity);
                    newNode.parent = parentNode; // 建立父级引用
                    newNode.isExpanded = true;   // 新节点默认展开（如果它以后有成员）

                    // 2. 数据层同步：将新实体加入父实体的 children
                    if (parentNode.getData().getChildren() == null) {
                        parentNode.getData().setChildren(new java.util.ArrayList<>());
                    }
                    parentNode.getData().getChildren().add(newEntity);

                    // 3. 逻辑层同步：将新包装器加入父包装器的 children
                    if (parentNode.children == null) {
                        parentNode.children = new java.util.ArrayList<>();
                    }
                    parentNode.children.add(newNode);

                    // 4. 索引同步：将新节点加入全局列表和 Map 索引
                    layoutNodes.add(newNode);
                    nodeMap.put(newEntity.getId(), newNode); // 如果你使用了 Map 索引

                    // 5. 交互优化：确保父节点处于展开状态，否则看不见新节点
                    parentNode.isExpanded = true;

                    // 6. 重新布局并绘图
                    calculateLayout();
                    redraw();

                    ClientContext.get().toast(0, 0, "团队 " + newEntity.getName() + " 创建成功");

                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });


    }

    private void onDeleteTeam(TeamGroupNode layoutNode) {
        DevProjectTeamEntity team = layoutNode.getData();
        String msg = "确定要删除小组 [" + team.getName() + "] 及其所有子分组吗？";

        ClientContext.get().confirmDelete(msg).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
            @Override
            public @Nullable IThenable<Object> onInvoke(Void p0) {
                doDeleteTeam(layoutNode);
                return null;
            }
        });
    }

    private void doDeleteTeam(TeamGroupNode nodeToDelete) {
        // 构造请求调用 AppProxy.get().deleteProjectTeam...
        // 成功后，从父节点的 children 中移除并刷新
        // 示例：findAndRemoveFromTree(rootTeams, nodeToDelete.getData().getId());
        // calculateLayout(); redraw();
    }

    private void onAddMember(DevProjectTeamEntity team) {
        ClientContext.get().chooseUser().then(new IThenable.ThenOnFulfilledCallbackFn<JsArray<IUserInfo>, Object>() {
            @Override
            public @Nullable IThenable<Object> onInvoke(JsArray<IUserInfo> p0) {
                if (p0.length > 0) {
                    doAddMember(team, p0.getAt(0));
                }
                return null;
            }
        });
    }

    private void doAddMember(final DevProjectTeamEntity team, @Nullable IUserInfo user) {
        UpdateProjectMemberRequest request = new UpdateProjectMemberRequest();
        DevProjectTeamMemberEntity member = new DevProjectTeamMemberEntity();
        member.setProjectId(team.getProjectId());
        member.setTeamId(team.getId());
        member.setUserId(Long.parseLong(user.getId()));
        member.setPermission(CommonPermission.fromPermission(0).getPermission());
        member.setSummary("");
        request.setMember(member);
        AppProxy.get().updateProjectMember(request, new AsyncCallback<RpcResult<UpdateProjectMemberResponse>>() {
            @Override
            public void onFailure(Throwable caught) {

            }

            @Override
            public void onSuccess(RpcResult<UpdateProjectMemberResponse> result) {
                ProjectMember member1 = result.getData().getMember();
                team.getMembers().add(member1);
                calculateLayout();
                redraw();
            }
        });
    }

    private void calculateLayout() {
        if (rootNodes.isEmpty()) return;

        // 1. 初始化边界：用第一个节点初始化，而不是 Double.MAX
        TeamGroupNode first = rootNodes.get(0);
        contentBounds.set(0, 0, 0, 0);

        // 2. 递归布局
        computeTreeGeometry(rootNodes, 0, 0);
        syncNodes();
    }

    private void updateBounds(TeamGroupNode node) {
        // 如果是第一个节点，直接复制，否则扩展
        if (contentBounds.width == 0 && contentBounds.height == 0) {
            contentBounds.copyFrom(node.rect);
        } else {
            contentBounds.extend(node.rect);
        }
    }

    private double computeTreeGeometry(List<TeamGroupNode> nodes, double x, double y) {
        double currentLevelY = y;

        for (TeamGroupNode node : nodes) {
            double h = node.getDesiredHeight(TITLE_HEIGHT, MEMBER_LINE_HEIGHT, PADDING_BOTTOM);
            // 先赋予标准位置
            node.rect.set(x, currentLevelY, NODE_WIDTH, h);

            if (node.children != null && !node.children.isEmpty()) {
                // 递归子树
                double subTreeTotalHeight = computeTreeGeometry(node.children, x + NODE_WIDTH + LEVEL_GAP, currentLevelY);

                // 核心：父节点垂直对齐到子树的几何中心
                double subTreeCenterY = currentLevelY + (subTreeTotalHeight / 2.0);
                node.rect.y = subTreeCenterY - (node.rect.height / 2.0);

                // 下一个同级节点的起始 Y：必须跳过整个子树的高度或父节点高度（取大者）
                currentLevelY += Math.max(node.rect.height, subTreeTotalHeight) + NODE_GAP;
            } else {
                // 叶子节点，直接累加高度
                currentLevelY += node.rect.height + NODE_GAP;
            }

            // 更新全局边界包围盒
            updateBounds(node);
        }
        return currentLevelY - y; // 返回本层级占用的总垂直高度
    }

    private void preloadAvatar(TeamGroupNode node) {
        String url = AppResource.INSTANCE.noData().getSafeUri().asString();
        if (node.data.getCharger() != null && node.data.getMembers() != null) {
            for (ProjectMember m : node.data.getMembers()) {
                if (node.data.getCharger().equals(m.getUserId())) {
                    url = m.getAvatar();
                    break;
                }
            }
        }
        if (StringUtil.isBlank(url)) {
            url = AppResource.INSTANCE.emptyAvatar().getSafeUri().asString();
        }
        // 简单的缓存判断
        if (node.chargeImage != null && url.equals(node.chargeImage.src)) {
            return;
        }

        HTMLImageElement img = (HTMLImageElement) DomGlobal.document.createElement("img");
        img.src = url;
        img.onload = (e) -> {
            redraw();
            return null;
        };
        node.chargeImage = img;
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        setContinueDraw(false);
    }


    public void zoomToFit() {
        if (layoutNodes.isEmpty()) return;

        // 1. 获取内容的绝对边界
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

        for (TeamGroupNode node : layoutNodes) {
            Rect r = node.rect;
            minX = Math.min(minX, r.x);
            minY = Math.min(minY, r.y);
            maxX = Math.max(maxX, r.x + r.width);
            maxY = Math.max(maxY, r.y + r.height);
        }

        double contentW = maxX - minX;
        double contentH = maxY - minY;
        double canvasW = getOffsetWidth();
        double canvasH = getOffsetHeight();

        if (canvasW <= 0 || canvasH <= 0) return;

        // 2. 计算缩放比 (留出 10% 的边距)
        double padding = 40;
        double scaleX = (canvasW - padding * 2) / contentW;
        double scaleY = (canvasH - padding * 2) / contentH;
        currentScale = Math.min(1.0, Math.min(scaleX, scaleY));

        // 3. 计算偏移量：这是解决“偏向右下”的关键
        // 我们需要把内容的中心点 (minX + contentW/2)
        // 移动到 Canvas 的中心点 (canvasW/2)
        translation.x = (canvasW / 2.0) - (minX + contentW / 2.0) * currentScale;
        translation.y = (canvasH / 2.0) - (minY + contentH / 2.0) * currentScale;
    }

    public void syncSize() {
        int newW = getOffsetWidth();
        int newH = getOffsetHeight();
        // 只有尺寸真正变化时才重置画布空间
        if (newW != getCoordinateSpaceWidth() || newH != getCoordinateSpaceHeight()) {
            double dpr = DomGlobal.window.devicePixelRatio;
            setCoordinateSpaceWidth((int) (newW * dpr));
            setCoordinateSpaceHeight((int) (newH * dpr));
            redraw();
        }
    }

    /**
     * 切换全局展开/折叠状态
     *
     * @param expand true 为全部展开，false 为全部收起
     */
    public void toggleAllNodes(boolean expand) {
        if (layoutNodes == null || layoutNodes.isEmpty()) return;

        for (TeamGroupNode node : layoutNodes) {
            node.setExpanded(expand);
        }

        // 关键：状态改变后需要重新计算布局，因为节点高度变了
        calculateLayout();
        // 重新绘图
        redraw();
    }

    @Override
    protected void onDraw(double timestamp) {
        CanvasRenderingContext2D ctx = Js.uncheckedCast(getContext2d());
        double dpr = DomGlobal.window.devicePixelRatio;

        ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
        ctx.clearRect(0, 0, getOffsetWidth(), getOffsetHeight());

        if (layoutNodes.isEmpty()) return;

        ctx.save();
        ctx.translate(translation.x, translation.y);
        ctx.scale(currentScale, currentScale);

        // 1. 先画线 (连线在底层)
        for (TeamGroupNode node : layoutNodes) {
            drawLinks(ctx, node);
        }

        // 2. 遍历节点并调用它们自己的 draw 方法
        TeamGroupNode activeDragging = null;
        for (TeamGroupNode node : layoutNodes) {
            if (node.isBeingDragged()) {
                activeDragging = node;
                continue;
            }

            // 1. 先画节点本身
            node.draw(ctx);

            // 2. 核心修复：如果当前节点是拖拽目标，且正在拖拽中，根据合法性画叠加层
            if (isDragging && node == hoverDropTarget) {
                if (isInvalidDropTarget(node)) {
                    // 调用你刚写在 LayoutNode 里的红色斜纹逻辑
                    node.drawInvalidTargetOverlay(ctx, node);
                } else {
                    // 调用你刚写在 LayoutNode 里的蓝色高亮逻辑
                    node.drawDropZoneHighlight(ctx);
                }
            }
        }

        // 3. 置顶绘制正在拖拽的节点
        if (activeDragging != null) {
            ctx.save();
            ctx.shadowBlur = 15;
            ctx.shadowColor = "rgba(0,0,0,0.2)";
            activeDragging.draw(ctx);
            ctx.restore();
        }

        ctx.restore();
    }

    private void drawLinks(CanvasRenderingContext2D ctx, TeamGroupNode p) {
        // 关键修正：删掉 !p.isExpanded 判断
        if (p.children == null || p.children.isEmpty()) return;

        // 起点：始终固定在父节点标题栏的右侧中心
        double startX = p.rect.x + p.rect.width;
        double startY = p.rect.y + TITLE_HEIGHT / 2.0;

        for (TeamGroupNode child : p.children) {
            // 终点：始终固定在子节点标题栏的左侧中心
            double endX = child.rect.x;
            double endY = child.rect.y + TITLE_HEIGHT / 2.0;

            ctx.beginPath();
            ctx.setLineDash(new double[]{}); // 确保是实线
            ctx.moveTo(startX, startY);

            double cp1x = startX + (endX - startX) * 0.5;
            double cp2x = startX + (endX - startX) * 0.5;

            ctx.bezierCurveTo(cp1x, startY, cp2x, endY, endX, endY);

            // 如果节点被选中，连线可以加深颜色
            ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of(p.isSelected() ? "#1890ff" : "#d9d9d9");
            ctx.lineWidth = 1.5;
            ctx.stroke();

            // 递归：继续画子节点的连线
            drawLinks(ctx, child);
        }
    }

    /**
     * 判断指定节点是否为非法放置目标
     */
    private boolean isInvalidDropTarget(TeamGroupNode target) {
        if (draggedNode == null) return false;

        // 1. 自身不能作为父节点
        if (target == draggedNode) return true;

        // 2. 当前的父节点（已经是父子关系了，没必要挂载）
        if (target.data.getId().equals(draggedNode.data.getParentId())) return true;

        // 3. 自己的子孙节点（最关键：防止循环引用）
        return isDescendantOf(target, draggedNode);
    }

    private DevProjectTeamEntity findAndRemoveFromTree(List<DevProjectTeamEntity> teams, String targetId) {
        if (teams == null || teams.isEmpty()) return null;

        for (int i = 0; i < teams.size(); i++) {
            DevProjectTeamEntity team = teams.get(i);
            if (team.getId().equals(targetId)) {
                return teams.remove(i);
            }
            DevProjectTeamEntity found = findAndRemoveFromTree(team.getChildren(), targetId);
            if (found != null) return found;
        }
        return null;
    }


    @Override
    public List<DevProjectTeamEntity> getData() {
        return rootTeams;
    }

    @Override
    public void setData(List<DevProjectTeamEntity> obj) {
        this.rootTeams = obj;
        this.layoutNodes.clear();
        this.nodeMap.clear();
        this.rootNodes.clear();

        // 第一步：构建纯粹的 Node 树
        this.rootNodes = buildTree(obj, null);

        // 第二步：物理布局计算（填充每个 node.rect）
        calculateLayout();

        // 3. 关键修复：延迟适配
        // 使用 GWT 的 Scheduler 确保在元素挂载并获得尺寸后再计算缩放
        Scheduler.get().scheduleDeferred(() -> {
            if (getOffsetWidth() > 0 && getOffsetHeight() > 0) {
                zoomToFit();
                redraw();
            } else {
                // 如果还是 0，尝试在下一帧重试，或者监听 Resize
                DomGlobal.setTimeout(ignore -> {
                    zoomToFit();
                    redraw();
                }, 100);
            }
        });
    }

    private List<TeamGroupNode> buildTree(List<DevProjectTeamEntity> teams, TeamGroupNode parentNode) {
        List<TeamGroupNode> currentLevelNodes = new ArrayList<>();
        if (teams == null) return currentLevelNodes;

        for (DevProjectTeamEntity team : teams) {
            TeamGroupNode node = new TeamGroupNode();
            node.data = team;
            node.parent = parentNode;
            node.rect.width = NODE_WIDTH; // 宽度固定

            nodeMap.put(team.getId(), node);
            layoutNodes.add(node);
            preloadAvatar(node);

            // 递归构建子树
            node.children = buildTree(team.getChildren(), node);
            currentLevelNodes.add(node);
        }
        return currentLevelNodes;
    }


    /**
     * 在逻辑坐标系下寻找潜在的父节点
     */
    private TeamGroupNode findPotentialParent(TeamGroupNode movingNode) {
        // 遍历所有节点，寻找一个“不是我自己”且“包含我左边缘中心点”的节点
        Size leftCenter = new Size(movingNode.rect.x, movingNode.rect.y + TITLE_HEIGHT / 2.0);

        for (TeamGroupNode target : layoutNodes) {
            if (target == movingNode) continue;

            // 如果移动节点的左中心进入了目标节点的矩形范围（或者稍微扩大的感应区）
            if (target.rect.contains(leftCenter.x, leftCenter.y)) {
                return target;
            }
        }
        return null;
    }

    /**
     * 递归检查 potential 是否为 target 的后代
     */
    private boolean isDescendantOf(TeamGroupNode potential, TeamGroupNode target) {
        String parentId = potential.data.getParentId();
        if (StringUtil.isBlank(parentId)) return false;

        // 如果潜在父节点的父 ID 就是当前拖拽节点，说明它是后代
        if (parentId.equals(target.data.getId())) return true;

        // 向上追溯
        TeamGroupNode parent = nodeMap.get(parentId);
        if (parent != null) {
            return isDescendantOf(parent, target);
        }
        return false;
    }
}