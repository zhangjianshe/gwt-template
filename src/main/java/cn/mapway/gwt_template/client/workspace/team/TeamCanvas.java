package cn.mapway.gwt_template.client.workspace.team;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.workspace.widget.ActionMenu;
import cn.mapway.gwt_template.client.workspace.widget.ActionMenuKind;
import cn.mapway.gwt_template.shared.db.DevProjectTeamEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTeamRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTeamResponse;
import cn.mapway.ui.client.IUserInfo;
import cn.mapway.ui.client.mvc.Rect;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.canvas.CanvasWidget;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.HasCommonHandlers;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.Callback;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import elemental2.core.JsArray;
import elemental2.dom.BaseRenderingContext2D;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.promise.IThenable;
import jsinterop.base.Js;
import lombok.Getter;
import org.jspecify.annotations.Nullable;


/**
 * 团队架构画布 - 优化版
 * 功能：平移缩放、树状布局、左键选中、右键菜单、动态鼠标样式
 */
public class TeamCanvas extends CanvasWidget implements IData<String>, HasCommonHandlers {
    public final Size translation = new Size(0, 0); // 替代 translateX, translateY
    // 交互辅助
    private final Size lastMousePos = new Size(0, 0);   // 上一次鼠标位置 (屏幕坐标)
    private final Size startMousePos = new Size(0, 0);  // 按下时的鼠标位置 (屏幕坐标)
    private final Size currentMouse = new Size(0, 0);
    public double currentScale = 1.0;
    TeamCanvasData data;
    TeamHitResult hitOrigin = new TeamHitResult();
    TeamHitResult hitCurrent = new TeamHitResult();
    TeamHitResult hitLast = new TeamHitResult();
    ActionMenu menuNode = new ActionMenu();
    ActionMenu menuCanvas = new ActionMenu();
    boolean mouseDown = false;
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
                    case AMK_EDIT_GROUP:
                        onEditTeam(menuNode.getData());
                        break;
                    case EXPORT_TO_PICTURE:
                        onExportToPicture();
                        break;
                    case ZOOM_TO_FIT:
                        onZoomToFit();
                        break;
                    default:
                }

            }
            actionMenu.hide();
        }
    };
    @Getter
    private boolean readonly = false;
    private ActionMode actionMode = ActionMode.ACTION_MODE_DEFAULT;
    public TeamCanvas() {
        super();
        data = new TeamCanvasData(this);

        Style style = getElement().getStyle();
        style.setProperty("cursor", "grab");
        style.setProperty("userSelect", "none");
        style.setProperty("webkitUserSelect", "none");

        // 节点菜单
        menuNode.addItem("➕<span>添加子分组</span>", ActionMenuKind.AMK_ADD_GROUP);
        menuNode.addItem("📝<span>编辑分组</span>", ActionMenuKind.AMK_EDIT_GROUP); // 新增编辑功能
        menuNode.addItem("🗑️<span>删除分组</span>", ActionMenuKind.AMK_DELETE);
        menuNode.addSeparator();
        menuNode.addItem("👤<span>添加人员</span>", ActionMenuKind.AMK_ADD_MEMBER);

        menuNode.addCommonHandler(menuNodeHandler);
        menuNode.addCloseHandler(event -> {
            redraw();
        });
        menuCanvas.addItem("🖼️<span>导出图片</span>", ActionMenuKind.EXPORT_TO_PICTURE);
        menuCanvas.addItem("🎯<span>复位视图</span>", ActionMenuKind.ZOOM_TO_FIT);
        menuCanvas.addCommonHandler(menuNodeHandler);
        menuCanvas.addCloseHandler(event -> {
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
            event.stopPropagation();
            mouseDown = true;

            double rx = event.getRelativeX(getElement());
            double ry = event.getRelativeY(getElement());
            currentMouse.set(rx, ry);
            Size logicPos = toLogicPos(rx, ry);

            // 【修复点】无论什么模式，lastMousePos 必须存物理坐标 (rx, ry)
            lastMousePos.set(rx, ry);

            // 命中检测
            data.hitTest(hitOrigin, logicPos.x, logicPos.y, null);
            hitLast.clear();
            hitCurrent.clear();

            if (readonly) {
                // --- 只读模式下的特殊逻辑 ---
                if (event.getNativeButton() == NativeEvent.BUTTON_RIGHT) {
                    // 允许只读模式下弹出右键菜单
                    actionMode = ActionMode.ACTION_MODE_MENU;
                    handleRightClick(hitOrigin, rx + 3, ry + 3);
                    return;
                }
                if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
                    if (hitOrigin.hitArea == TeamHitTest.AREA_BTN_DROPDOWN) {
                        actionMode = ActionMode.ACTION_MODE_DROPDOWN;
                    } else {
                        // 只读模式下，点击任何地方（包括成员）都只触发画布平移
                        actionMode = ActionMode.ACTION_MODE_DEFAULT;
                        setCursor("grabbing");
                        DOM.setCapture(getElement());
                    }
                }
                return; // 关键：此处的 return 防止了进入下方的非只读逻辑
            }

            // 命中检测
            data.hitTest(hitOrigin, logicPos.x, logicPos.y, null);

            hitLast.clear();
            hitCurrent.clear();

            // --- 处理右键点击 ---
            if (event.getNativeButton() == NativeEvent.BUTTON_RIGHT) {
                actionMode = ActionMode.ACTION_MODE_MENU;
                handleRightClick(hitOrigin, rx + 3, ry + 3);
                return;
            }

            switch (hitOrigin.hitArea) {
                case AREA_BTN_DROPDOWN:
                    actionMode = ActionMode.ACTION_MODE_DROPDOWN;
                    break;

                case AREA_MEMBER_ITEM:
                    data.clearSelection();
                    hitOrigin.sourceNode.setSelected(true);
                    hitCurrent.copyFrom(hitOrigin);
                    actionMode = ActionMode.ACTION_MODE_DRAGGING_MEMBER;
                    DOM.setCapture(getElement());
                    break;

                case AREA_BODY:
                    startDraggingNode(logicPos);
                    break;

                case NONE:
                    data.clearSelection();
                    actionMode = ActionMode.ACTION_MODE_DEFAULT;
                    startPanningCanvas(logicPos);
                    lastMousePos.set(rx, ry);
                    break;
            }
            redraw();
        });

        addMouseMoveHandler(event -> {

            // 统一使用相对于元素的坐标
            double mx = event.getRelativeX(getElement());
            double my = event.getRelativeY(getElement());
            Size logicPos = toLogicPos(mx, my);
            if (readonly) {
                if (mouseDown) {
                    if (actionMode == ActionMode.ACTION_MODE_DEFAULT) {
                        handleCanvasPanningMove(mx, my);
                    }
                } else {
                    // 允许在只读模式下显示 Hover 效果（仅限按钮和指针）
                    handlePassiveHover(logicPos);
                }
                lastMousePos.set(mx, my);
                redraw();
                return;
            }
            if (!mouseDown) {
                return;
            }

            // 1. 性能守卫：微小移动不触发计算
            if (Math.abs(mx - lastMousePos.x) < 0.5 && Math.abs(my - lastMousePos.y) < 0.5) {
                return;
            }
            currentMouse.set(mx, my);

            switch (actionMode) {
                case ACTION_MODE_MENU:
                case ACTION_MODE_DROPDOWN:
                    break;
                case ACTION_MODE_DRAGGING_NODE:
                    data.hitTest(hitCurrent, logicPos.x, logicPos.y, hitOrigin.sourceNode);
                    handleNodeDraggingMove(logicPos);
                    break;
                case ACTION_MODE_DRAGGING_MEMBER:
                    data.hitTest(hitCurrent, logicPos.x, logicPos.y, null);
                    handleMemberDraggingMove();
                    break;
                case ACTION_MODE_DEFAULT:
                    handleCanvasPanningMove(mx, my);
                    break;
                default:

            }
            redraw();
            lastMousePos.set(mx, my); // 统一更新
        });

        // 5. 鼠标抬起
        addMouseUpHandler(event -> {
            event.preventDefault();
            event.stopPropagation();
            double mx = event.getRelativeX(getElement());
            double my = event.getRelativeY(getElement());
            Size logicPos = toLogicPos(mx, my);
            mouseDown = false;
            DOM.releaseCapture(getElement());
            switch (actionMode) {
                case ACTION_MODE_DROPDOWN:
                    data.hitTest(hitCurrent, logicPos.x, logicPos.y, null);
                    if (hitCurrent.hitArea == TeamHitTest.AREA_BTN_DROPDOWN && hitCurrent.isSameNode(hitOrigin)) {
                        hitOrigin.sourceNode.setExpanded(!hitOrigin.sourceNode.isExpanded());
                        data.calculateLayout(null);
                    }
                    break;
                case ACTION_MODE_DRAGGING_NODE:
                    processNodeDrop();
                    break;
                case ACTION_MODE_DRAGGING_MEMBER:
                    processMemberDrop();
                    break;
                case ACTION_MODE_DEFAULT:
                    break;
                default:
            }
            resetInteractionState();
            redraw();
        });
    }

    public void withContext(CanvasRenderingContext2D ctx, Runnable action) {
        ctx.save(); // 保存当前画笔状态
        try {
            action.run();
        } finally {
            ctx.restore(); // 无论如何都要恢复，防止污染下一个节点
        }
    }

    private void onZoomToFit() {
        zoomToFit();
    }

    private void onExportToPicture() {
        if (data.isEmpty()) return;

        // 1. 计算内容的逻辑总边界
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
        for (TeamGroupNode node : data.getFlatNodes()) {
            Rect r = node.getRect();
            minX = Math.min(minX, r.x);
            minY = Math.min(minY, r.y);
            maxX = Math.max(maxX, r.x + r.width);
            maxY = Math.max(maxY, r.y + r.height);
        }

        double padding = 50;
        double contentW = (maxX - minX) + padding * 2;
        double contentH = (maxY - minY) + padding * 2;

        // 2. 创建一个离屏 Canvas
        elemental2.dom.HTMLCanvasElement offscreen = (elemental2.dom.HTMLCanvasElement)
                DomGlobal.document.createElement("canvas");
        offscreen.width = (int) contentW;
        offscreen.height = (int) contentH;
        CanvasRenderingContext2D ctx = Js.uncheckedCast(offscreen.getContext("2d"));

        // 3. 在离屏 Canvas 上重绘所有内容
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#ffffff");
        ctx.fillRect(0, 0, contentW, contentH);

        ctx.save();
        // 将坐标系移到内容起点，并留出边距
        ctx.translate(-minX + padding, -minY + padding);

        // 绘制连线
        for (TeamGroupNode node : data.getFlatNodes()) {
            drawLinks(ctx, node); // 复用你的 drawLinks 方法
        }
        // 绘制节点
        for (TeamGroupNode node : data.getFlatNodes()) {
            node.draw(ctx); // 复用你的 draw 方法
        }
        ctx.restore();


        // 3. 转换为 Data URL (PNG 格式)
        String dataUrl = offscreen.toDataURL("image/png");

        // 4. 创建隐藏的下载链接并触发
        elemental2.dom.HTMLAnchorElement link = (elemental2.dom.HTMLAnchorElement)
                DomGlobal.document.createElement("a");
        link.href = dataUrl;
        link.download = "Team_Architecture_" + StringUtil.formatDate(new java.util.Date(), "yyyyMMdd_HHmmss") + ".png";

        DomGlobal.document.body.appendChild(link);
        link.click();
        DomGlobal.document.body.removeChild(link);

        ClientContext.get().toast(0, 0, "图片导出成功");
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
        // 切换模式时重置交互状态并重绘
        resetInteractionState();
        if (readonly) {
            setCursor("default");
        } else {
            setCursor("grab");
        }
        redraw();
    }

    private void onEditTeam(TeamGroupNode layoutNode) {
        DevProjectTeamEntity team = layoutNode.getData();

        // 弹出输入框，初始值为当前名称
        ClientContext.get().input("编辑小组信息", "小组名称", "请输入新的名称", team.getName(), new Callback() {
            @Override
            public void onFailure(Object reason) {
            }

            @Override
            public void onSuccess(Object result) {
                String newName = (String) result;
                if (StringUtil.isBlank(newName) || newName.equals(team.getName())) {
                    return;
                }

                // 构造请求
                UpdateProjectTeamRequest request = new UpdateProjectTeamRequest();
                DevProjectTeamEntity updateEntity = new DevProjectTeamEntity();
                updateEntity.setId(team.getId());
                updateEntity.setProjectId(team.getProjectId());
                updateEntity.setName(newName);
                request.setProjectTeam(updateEntity);

                AppProxy.get().updateProjectTeam(request, new AsyncCallback<RpcResult<UpdateProjectTeamResponse>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        ClientContext.get().toast(0, 0, caught.getMessage());
                    }

                    @Override
                    public void onSuccess(RpcResult<UpdateProjectTeamResponse> result) {
                        if (result.isSuccess()) {
                            // 直接更新本地数据并重新绘图，无需 rebuild 全量数据
                            team.setName(newName);
                            redraw();
                            ClientContext.get().toast(0, 0, "更新成功");
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onBrowserEvent(com.google.gwt.user.client.Event event) {
        if (DOM.eventGetType(event) == com.google.gwt.user.client.Event.ONCONTEXTMENU) {
            event.preventDefault();
            event.stopPropagation();
            return; // 直接拦截，不向下传递
        }
        super.onBrowserEvent(event);
    }

    private void processMemberDrop() {
        if (hitCurrent == null) return;
        if (hitCurrent.hitArea == null) return;
        switch (hitCurrent.hitArea) {
            case AREA_CHARGE:
                data.changeNodeCharge(hitOrigin, hitCurrent);
                break;
            case AREA_MEMBER_ITEM:
            case AREA_BODY:
                data.moveMember(hitOrigin, hitCurrent);
                break;
            case NONE:
                data.moveMember(hitOrigin, null);
                break;
            default:
        }

    }


    /**
     * 拖动一个成员在画布上移动
     */
    private void handleMemberDraggingMove() {
        for (TeamGroupNode node : data.getFlatNodes()) {
            node.resetAllEffect();
        }
        switch (hitCurrent.hitArea) {
            case AREA_MEMBER_ITEM:
            case AREA_BODY:
                if (hitOrigin.isSameNode(hitCurrent)) {
                    hitCurrent.sourceNode.setDenyMemberEffect(false);
                    hitCurrent.sourceNode.setAcceptMemberEffect(false);
                    hitCurrent.sourceNode.setAcceptChargerEffect(false);
                } else {
                    hitCurrent.sourceNode.setDenyMemberEffect(false);
                    hitCurrent.sourceNode.setAcceptMemberEffect(true);
                    hitCurrent.sourceNode.setAcceptChargerEffect(false);
                }
                break;
            case AREA_CHARGE:
                hitCurrent.sourceNode.setDenyMemberEffect(false);
                hitCurrent.sourceNode.setAcceptMemberEffect(false);
                hitCurrent.sourceNode.setAcceptChargerEffect(true);
                break;
            case NONE:
        }

    }

    private void processNodeDrop() {
        hitLast.clear();
        switch (hitCurrent.hitArea) {
            case AREA_BODY:
            case AREA_MEMBER_ITEM:
            case AREA_CHARGE:
            case AREA_BTN_DROPDOWN:
                if (!data.isInvalidDropTarget(hitOrigin.sourceNode, hitCurrent.sourceNode)) {
                    data.doChangeTeamParent(hitOrigin.sourceNode, hitCurrent.sourceNode);
                }
                break;
            default:
        }
        data.calculateLayout(data.getFirstNode());
    }


    private void resetInteractionState() {
        hitOrigin.clear();
        hitCurrent.clear();
        hitLast.clear();
        for (TeamGroupNode node : data.getFlatNodes()) {
            node.setBeingDragged(false);
            node.resetAllEffect();
        }
        // 6. 恢复光标样式
        setCursor("default");
    }


    private void setCursor(String cursorStyle) {
        // 只有当样式确实发生变化时才操作 DOM，减少性能损耗
        String current = getElement().getStyle().getProperty("cursor");
        if (!cursorStyle.equals(current)) {
            getElement().getStyle().setProperty("cursor", cursorStyle);
        }
    }


    private void handleCanvasPanningMove(double mx, double my) {
        // mx 和 my 是当前鼠标物理位置，lastMousePos 是上一帧物理位置
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
        if (readonly) {
            // 只读模式下，无论点哪里都显示全景画布菜单
            showGlobalContextMenu(sx, sy);
            return;
        }

        // --- 非只读模式的原有逻辑 ---
        if (hit.sourceNode != null) {
            data.clearSelection();
            hit.sourceNode.setSelected(true);
            redraw();
        }

        switch (hit.hitArea) {
            case AREA_BODY:
                showTeamActionMenu(hit, sx, sy);
                break;
            case NONE:
            case AREA_MEMBER_ITEM: // 也可以根据需要为成员增加只读菜单
                showGlobalContextMenu(sx, sy);
                break;
        }
    }

    private void showGlobalContextMenu(double x, double y) {
        // 计算绝对位置弹出
        menuCanvas.setPopupPosition((int) x + getAbsoluteLeft(), (int) y + getAbsoluteTop());
        menuCanvas.show();
    }

    private void startPanningCanvas(Size locPosition) {
        this.lastMousePos.copyFrom(locPosition);
        setCursor("grabbing");
        DOM.setCapture(getElement());
    }

    /**
     * 用户鼠标点击了一个NODE,有可能进行两个操作 一个是选中 一个是拖动
     *
     * @param logicPos 鼠标位置
     */
    private void startDraggingNode(Size logicPos) {
        if (hitOrigin.isClear()) return;

        actionMode = ActionMode.ACTION_MODE_DRAGGING_NODE;
        data.clearSelection();
        TeamGroupNode node = hitOrigin.sourceNode;
        node.setSelected(true);
        node.setBeingDragged(false);

        // 捕获交互并更新样式
        setCursor("grabbing");
        DOM.setCapture(getElement());

        // 记录拖拽开始时的位置，用于 5px 阈值判定
        // 注意：这里用屏幕坐标或相对坐标都可以，只要 MouseMove 判定时一致即可
        this.startMousePos.set(logicPos.x, logicPos.y);

        redraw();
    }


    private void handleNodeDraggingMove(Size logicPos) {
        // 5像素阈值检测：防止点击时的轻微抖动误触发拖拽
        TeamGroupNode originNode = hitOrigin.sourceNode;
        if (startMousePos.distanceTo(logicPos.x, logicPos.y) > 5) {
            originNode.setBeingDragged(true);
        }

        if (originNode.isBeingDragged()) {
            // 更新节点物理坐标
            Rect rect = originNode.getRect();
            rect.offset(logicPos.x - startMousePos.x, logicPos.y - startMousePos.y);
            startMousePos.copyFrom(logicPos);

            for (TeamGroupNode node : data.getFlatNodes()) {
                node.resetAllEffect();
            }
            //移动过程中 hitCurrent是鼠标下的目标
            switch (hitCurrent.hitArea) {
                case AREA_MEMBER_ITEM:
                case AREA_CHARGE:
                case AREA_BTN_DROPDOWN:
                case AREA_BODY:

                    // 检查 鼠标下的节点可不可以作为
                    if (data.isInvalidDropTarget(originNode, hitCurrent.sourceNode)) {
                        hitCurrent.sourceNode.setDenyNodeEffect(true);
                        hitCurrent.sourceNode.setAcceptNodeEffect(false);

                        setCursor("not-allowed");
                    } else {
                        hitCurrent.sourceNode.setDenyNodeEffect(false);
                        hitCurrent.sourceNode.setAcceptNodeEffect(true);
                        setCursor("grabbing");
                    }
                    break;
                case NONE:
                default:
            }
        }
    }


    private void handlePassiveHover(Size lp) {
        for (TeamGroupNode n : data.getFlatNodes()) {
            n.resetAllEffect();
        }
        data.hitTest(hitCurrent, lp.x, lp.y, null);

        switch (hitCurrent.hitArea) {
            case AREA_BTN_DROPDOWN:
                hitCurrent.sourceNode.setHoverOnDropdownButton(true);
                setCursor("pointer");
                break;
            case AREA_BODY:
            case AREA_MEMBER_ITEM:
                // 只读模式下，指向成员或身体也可以显示为可抓取状态，提示可以平移
                setCursor(readonly ? "default" : "grab");
                break;
            default:
                setCursor("default");
        }
        redraw();
    }


    private void showTeamActionMenu(TeamHitResult result, double x, double y) {
        // 计算绝对位置弹出
        menuNode.setPopupPosition((int) x + getAbsoluteLeft(), (int) y + getAbsoluteTop());
        menuNode.setData(result.sourceNode);
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
                data.doAddTeam(layoutNode, teamName);
            }
        });
    }


    private void onDeleteTeam(TeamGroupNode teamNode) {
        DevProjectTeamEntity team = teamNode.getData();
        String msg = "确定要删除小组 [" + team.getName() + "] 及其所有子分组吗？";

        ClientContext.get().confirmDelete(msg).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
            @Override
            public @Nullable IThenable<Object> onInvoke(Void p0) {
                data.doDeleteTeam(teamNode);
                return null;
            }
        });
    }


    private void onAddMember(DevProjectTeamEntity team) {
        ClientContext.get().chooseUser().then(new IThenable.ThenOnFulfilledCallbackFn<JsArray<IUserInfo>, Object>() {
            @Override
            public @Nullable IThenable<Object> onInvoke(JsArray<IUserInfo> p0) {
                if (p0.length > 0) {
                    data.doAddMember(team, p0.getAt(0));
                }
                return null;
            }
        });
    }


    @Override
    protected void onLoad() {
        super.onLoad();
        Element element = Js.uncheckedCast(getElement());
        element.addEventListener("contextmenu", (e) -> {
            e.preventDefault();
        });
        setContinueDraw(false);
    }


    public void zoomToFit() {
        if (data.isEmpty()) return;

        // 1. 获取内容的绝对边界
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

        for (TeamGroupNode node : data.getFlatNodes()) {
            Rect r = node.getRect();
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
        if (data.isEmpty()) return;
        for (TeamGroupNode node : data.getFlatNodes()) {
            node.setExpanded(expand);
        }
        // 关键：状态改变后需要重新计算布局，因为节点高度变了
        data.calculateLayout(null);
    }

    @Override
    protected void onDraw(double timestamp) {
        CanvasRenderingContext2D ctx = Js.uncheckedCast(getContext2d());
        double dpr = DomGlobal.window.devicePixelRatio;

        ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
        ctx.clearRect(0, 0, getOffsetWidth(), getOffsetHeight());

        if (data.isEmpty()) return;

        ctx.save();
        ctx.translate(translation.x, translation.y);
        ctx.scale(currentScale, currentScale);

        for (TeamGroupNode node : data.getFlatNodes()) {
            drawLinks(ctx, node);
        }

        TeamGroupNode activeDragging = null;
        for (TeamGroupNode node : data.getFlatNodes()) {
            if (node.isBeingDragged()) {
                activeDragging = node;
                continue;
            }
            node.draw(ctx);
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
        if (!readonly) {
            switch (hitOrigin.hitArea) {
                case AREA_MEMBER_ITEM: {
                    if (hitOrigin.sourceMember == null) {
                        return;
                    }
                    double ghostX = currentMouse.x + 10;
                    double ghostY = currentMouse.y + 10;
                    drawGhost(ctx, ghostX, ghostY);
                }
            }
        }

    }

    private void drawGhost(CanvasRenderingContext2D ctx, double ghostX, double ghostY) {
        withContext(ctx, () -> {
            ctx.globalAlpha = 0.9; // 稍微调高不透明度，增强可读性
            ctx.shadowBlur = 10;
            ctx.shadowColor = "rgba(0,0,0,0.15)";

            // 1. 绘制背景
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#ffffff");
            ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#1890ff");
            ctx.lineWidth = 1;
            BaseNode.drawRoundedRect(ctx, ghostX, ghostY, 120, 45, 4);
            ctx.fill();
            ctx.stroke();

            // 3. 绘制文字
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#333333");
            ctx.font = "500 13px sans-serif";
            ctx.textBaseline = "middle";
            // 如果名字过长，虚影也支持省略号

            String subText = "";
            BaseNode.fillTextWithEllipsis(ctx, hitOrigin.sourceMember.getNickName(), ghostX + 12, ghostY + 15, 100);

            switch (hitCurrent.hitArea) {
                case AREA_CHARGE:
                    ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("blue");
                    subText = "设为小组长";
                    break;
                case AREA_MEMBER_ITEM:
                case AREA_BODY:
                case AREA_BTN_DROPDOWN:
                    if (!hitOrigin.isSameNode(hitCurrent)) {
                        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("green");
                        subText = "加入项目组";
                    } else {
                        subText = "不能操作";
                    }
                    break;
                case NONE:
                    if (!hitOrigin.isSameNode(hitCurrent)) {
                        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("red");
                        subText = "移出项目组";
                    } else {
                        subText = "不能操作";
                    }
                    break;
            }
            BaseNode.fillTextWithEllipsis(ctx, subText, ghostX + 12, ghostY + 32, 100);
        });
    }


    private void drawLinks(CanvasRenderingContext2D ctx, TeamGroupNode p) {
        if (p.getChildren() == null || p.getChildren().isEmpty()) return;

        Rect rect = p.getRect();
        double stubLength = 15.0; // 引线长度，可以根据视觉微调
        double startX = rect.x + rect.width;
        double startY = rect.y + p.getTitleHeight() / 2.0;

        for (TeamGroupNode child : p.getChildren()) {
            double endX = child.getRect().x;
            double endY = child.getRect().y + p.getTitleHeight() / 2.0;

            // 计算贝塞尔曲线的实际起止点（避开引线部分）
            double bStartX = startX + stubLength;
            double bEndX = endX - stubLength;

            ctx.beginPath();
            ctx.moveTo(startX, startY);

            // 1. 先画一段水平直引线
            ctx.lineTo(bStartX, startY);

            // 2. 绘制贝塞尔曲线到目标引线起点
            // 动态控制点：取剩余距离的 40% 作为控制点偏移量
            double cpOffset = (bEndX - bStartX) * 0.4;
            ctx.bezierCurveTo(
                    bStartX + cpOffset, startY,
                    bEndX - cpOffset, endY,
                    bEndX, endY
            );

            // 3. 画最后的水平直引线进入子节点
            ctx.lineTo(endX, endY);

            // 样式处理
            boolean highlight = p.isSelected() || child.isSelected();
            ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of(highlight ? "#1890ff" : "#d9d9d9");
            ctx.lineWidth = highlight ? 2.0 : 1.2;
            ctx.stroke();

            // 递归处理子节点
            drawLinks(ctx, child);
        }
    }

    @Override
    public String getData() {
        return data.getCurrentProjectId();
    }

    @Override
    public void setData(String projectId) {
        data.rebuild(projectId);
    }

    @Override
    public HandlerRegistration addCommonHandler(CommonEventHandler handler) {
        return addHandler(handler, CommonEvent.TYPE);
    }
}