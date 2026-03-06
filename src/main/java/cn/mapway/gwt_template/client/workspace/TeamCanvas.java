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
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.canvas.CanvasWidget;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.Callback;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import elemental2.core.JsArray;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLImageElement;
import elemental2.promise.IThenable;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 团队架构画布 - 优化版
 * 功能：平移缩放、树状布局、左键选中、右键菜单、动态鼠标样式
 */
public class TeamCanvas extends CanvasWidget implements IData<List<DevProjectTeamEntity>> {
    private static final int NODE_WIDTH = 180;
    private static final int NODE_HEIGHT = 60;
    private static final int LEVEL_GAP = 80;
    private static final int NODE_GAP = 20;
    private static final int CORNER_RADIUS = 8;
    private static final int AVATAR_SIZE = 36;

    private final List<LayoutNode> layoutNodes = new java.util.ArrayList<>();
    private final Map<String, LayoutNode> nodeMap = new HashMap<>();
    ActionMenu menuNode = new ActionMenu();
    private List<DevProjectTeamEntity> rootTeams;
    // 画布变换状态
    private double currentScale = 1.0;
    private double translateX = 0.0;
    private double translateY = 0.0;
    private double contentMinX, contentMinY, contentMaxX, contentMaxY;
    private double contentWidth = 0, contentHeight = 0;
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
    // 交互状态
    private double lastMouseX, lastMouseY;
    private boolean isDragging = false;
    private String contextMenuNodeId = null;

    public TeamCanvas() {
        super();
        getElement().getStyle().setProperty("cursor", "grab");
        menuNode.addItem("➕ 添加子分组", ActionMenuKind.AMK_ADD_GROUP);
        menuNode.addItem("👤 添加人员", ActionMenuKind.AMK_ADD_MEMBER);
        menuNode.addItem("🗑️ 删除分组", ActionMenuKind.AMK_DELETE);

        menuNode.addCommonHandler(menuNodeHandler);
        menuNode.addCloseHandler(event -> {
            contextMenuNodeId = null;
            redraw();
        });

        // 1. 缩放逻辑
        addDomHandler(event -> {
            event.preventDefault();
            double delta = event.getDeltaY() > 0 ? 0.9 : 1.1;
            double mouseX = event.getRelativeX(getElement());
            double mouseY = event.getRelativeY(getElement());

            double logicX = (mouseX - translateX) / currentScale;
            double logicY = (mouseY - translateY) / currentScale;

            currentScale = Math.max(0.1, Math.min(currentScale * delta, 3.0));
            translateX = mouseX - logicX * currentScale;
            translateY = mouseY - logicY * currentScale;

            redraw();
        }, MouseWheelEvent.getType());

        // 2. 右键菜单
        addDomHandler(event -> {
            event.preventDefault();
            event.stopPropagation();

            double mouseX = event.getNativeEvent().getClientX() - getAbsoluteLeft();
            double mouseY = event.getNativeEvent().getClientY() - getAbsoluteTop();

            handleContextMenu(mouseX, mouseY);
        }, ContextMenuEvent.getType());

        // 3. 鼠标按下 (区分左右键)
        addMouseDownHandler(event -> {
            int button = event.getNativeEvent().getButton();
            double mouseX = event.getRelativeX(getElement());
            double mouseY = event.getRelativeY(getElement());

            if (button == com.google.gwt.dom.client.NativeEvent.BUTTON_LEFT) {
                lastMouseX = mouseX;
                lastMouseY = mouseY;
                isDragging = true;
                com.google.gwt.user.client.DOM.setCapture(getElement());
                getElement().getStyle().setProperty("cursor", "grabbing");

                handleNodeSelection(mouseX, mouseY);
            } else {
                isDragging = false;
            }
        });

        // 4. 鼠标移动 (处理平移和光标样式)
        addMouseMoveHandler(event -> {
            double mouseX = event.getRelativeX(getElement());
            double mouseY = event.getRelativeY(getElement());

            if (isDragging) {
                translateX += (mouseX - lastMouseX);
                translateY += (mouseY - lastMouseY);
                lastMouseX = mouseX;
                lastMouseY = mouseY;
                redraw();
            } else {
                updateCursorStyle(mouseX, mouseY);
            }
        });

        // 5. 鼠标抬起
        addMouseUpHandler(event -> {
            isDragging = false;
            com.google.gwt.user.client.DOM.releaseCapture(getElement());
            // 抬起后根据当前位置重置样式
            updateCursorStyle(event.getRelativeX(getElement()), event.getRelativeY(getElement()));
        });
    }

    /**
     * 根据鼠标是否在 Node 上切换光标
     */
    private void updateCursorStyle(double screenX, double screenY) {
        double logicX = (screenX - translateX) / currentScale;
        double logicY = (screenY - translateY) / currentScale;

        boolean isOverNode = false;
        for (LayoutNode node : layoutNodes) {
            if (logicX >= node.x && logicX <= node.x + NODE_WIDTH &&
                    logicY >= node.y && logicY <= node.y + NODE_HEIGHT) {
                isOverNode = true;
                break;
            }
        }

        if (isOverNode) {
            getElement().getStyle().setProperty("cursor", "default");
        } else {
            getElement().getStyle().setProperty("cursor", "grab");
        }
    }

    private void handleNodeSelection(double screenX, double screenY) {
        double logicX = (screenX - translateX) / currentScale;
        double logicY = (screenY - translateY) / currentScale;

        boolean changed = false;
        for (LayoutNode node : layoutNodes) {
            boolean isInside = (logicX >= node.x && logicX <= node.x + NODE_WIDTH &&
                    logicY >= node.y && logicY <= node.y + NODE_HEIGHT);

            if (isInside) {
                if (!node.isSelected) {
                    clearAllSelections();
                    node.isSelected = true;
                    changed = true;
                    onNodeClicked(node.data);
                }
            }
        }
        if (changed) redraw();
    }

    private void clearAllSelections() {
        for (LayoutNode node : layoutNodes) node.isSelected = false;
    }

    private void handleContextMenu(double screenX, double screenY) {
        double logicX = (screenX - translateX) / currentScale;
        double logicY = (screenY - translateY) / currentScale;

        LayoutNode target = null;
        for (LayoutNode node : layoutNodes) {
            if (logicX >= node.x && logicX <= node.x + NODE_WIDTH &&
                    logicY >= node.y && logicY <= node.y + NODE_HEIGHT) {
                target = node;
                break;
            }
        }

        if (target != null) {
            contextMenuNodeId = target.data.getId();
            redraw();
            showTeamActionMenu(target, screenX, screenY);
        }
    }

    private void showTeamActionMenu(LayoutNode layoutNode, double x, double y) {
        // 计算绝对位置弹出
        menuNode.setPopupPosition((int) x + getAbsoluteLeft(), (int) y + getAbsoluteTop());
        menuNode.setData(layoutNode);
        menuNode.show();
    }

    private void onAddTeam(LayoutNode layoutNode) {
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

    private void doAddTeam(LayoutNode parentNode, String teamName) {
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
                    DevProjectTeamEntity projectTeam = result.getData().getProjectTeam();
                    LayoutNode node = new LayoutNode();
                    node.setData(projectTeam);
                    parentNode.getData().getChildren().add(projectTeam);

                    // 重新计算布局并绘图，不建议 zoomToFit 避免闪烁
                    calculateLayout();
                    redraw();
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });


    }

    private void onDeleteTeam(LayoutNode layoutNode) {
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

    private void doDeleteTeam(LayoutNode nodeToDelete) {
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
        layoutNodes.clear();
        nodeMap.clear();
        if (rootTeams == null || rootTeams.isEmpty()) return;

        contentMinX = contentMinY = Double.MAX_VALUE;
        contentMaxX = contentMaxY = Double.MIN_VALUE;
        measureAndLayout(rootTeams, 0, 0);
        contentWidth = contentMaxX - contentMinX;
        contentHeight = contentMaxY - contentMinY;
    }

    private double measureAndLayout(List<DevProjectTeamEntity> teams, double x, double y) {
        if (teams == null || teams.isEmpty()) return 0;
        double startY = y;
        double currentY = y;

        for (DevProjectTeamEntity team : teams) {
            LayoutNode node = new LayoutNode();
            node.data = team;
            node.x = x;
            node.y = currentY;
            layoutNodes.add(node);
            nodeMap.put(team.getId(), node);
            preloadAvatar(node);

            if (team.getChildren() != null && !team.getChildren().isEmpty()) {
                double chH = measureAndLayout(team.getChildren(), x + NODE_WIDTH + LEVEL_GAP, currentY);
                if (chH > NODE_HEIGHT) node.y = currentY + (chH / 2) - (NODE_HEIGHT / 2);
                currentY += Math.max(NODE_HEIGHT + NODE_GAP, chH + NODE_GAP);
            } else {
                currentY += (NODE_HEIGHT + NODE_GAP);
            }
            updateBounds(node);
        }
        return currentY - startY;
    }

    private void preloadAvatar(LayoutNode node) {
        String url = AppResource.INSTANCE.emptyAvatar().getSafeUri().asString();
        if (node.data.getCharger() != null && node.data.getMembers() != null) {
            for (ProjectMember m : node.data.getMembers()) {
                if (node.data.getCharger().equals(m.getUserId())) {
                    url = m.getAvatar();
                    break;
                }
            }
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

    private void updateBounds(LayoutNode node) {
        contentMinX = Math.min(contentMinX, node.x);
        contentMinY = Math.min(contentMinY, node.y);
        contentMaxX = Math.max(contentMaxX, node.x + NODE_WIDTH);
        contentMaxY = Math.max(contentMaxY, node.y + NODE_HEIGHT);
    }

    public void zoomToFit() {
        if (layoutNodes.isEmpty()) return;
        double pw = getOffsetWidth(), ph = getOffsetHeight();
        if (pw <= 0 || ph <= 0) return;
        currentScale = Math.min(Math.min((pw - 80) / contentWidth, (ph - 80) / contentHeight), 1.0);
        translateX = (pw / 2) - (contentMinX + contentWidth / 2) * currentScale;
        translateY = (ph / 2) - (contentMinY + contentHeight / 2) * currentScale;
        redraw();
    }

    public void syncSize() {
        double dpr = DomGlobal.window.devicePixelRatio;
        setCoordinateSpaceWidth((int) (getOffsetWidth() * dpr));
        setCoordinateSpaceHeight((int) (getOffsetHeight() * dpr));
        redraw();
    }

    @Override
    protected void onDraw(double timestamp) {
        Context2d ctx = getContext2d();
        double dpr = DomGlobal.window.devicePixelRatio;
        ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
        ctx.clearRect(0, 0, getOffsetWidth(), getOffsetHeight());

        if (layoutNodes.isEmpty()) return;
        ctx.save();
        ctx.translate(translateX, translateY);
        ctx.scale(currentScale, currentScale);

        for (LayoutNode node : layoutNodes) drawLinks(ctx, node);
        for (LayoutNode node : layoutNodes) drawNode(ctx, node);

        ctx.restore();
    }

    private void drawLinks(Context2d ctx, LayoutNode p) {
        if (p.data.getChildren() == null) return;
        ctx.setStrokeStyle("#cbd5e0");
        ctx.setLineWidth(1.2);
        for (DevProjectTeamEntity c : p.data.getChildren()) {
            LayoutNode cn = nodeMap.get(c.getId());
            if (cn != null) {
                ctx.beginPath();
                ctx.moveTo(p.x + NODE_WIDTH, p.y + NODE_HEIGHT / 2);
                double mx = p.x + NODE_WIDTH + (cn.x - (p.x + NODE_WIDTH)) / 2;
                ctx.lineTo(mx, p.y + NODE_HEIGHT / 2);
                ctx.lineTo(mx, cn.y + NODE_HEIGHT / 2);
                ctx.lineTo(cn.x, cn.y + NODE_HEIGHT / 2);
                ctx.stroke();
            }
        }
    }

    private void drawNode(Context2d ctx, LayoutNode node) {
        // 背景与边框
        drawRoundedRect(ctx, node.x, node.y, NODE_WIDTH, NODE_HEIGHT, CORNER_RADIUS);
        if (node.isSelected) {
            ctx.setFillStyle("#e6f7ff");
            ctx.setStrokeStyle("#1890ff");
            ctx.setLineWidth(2);
        } else {
            ctx.setFillStyle("#ffffff");
            ctx.setStrokeStyle("#d9d9d9");
            ctx.setLineWidth(1);
        }
        ctx.fill();
        ctx.stroke();

        // 头像
        double ax = node.x + 10, ay = node.y + (NODE_HEIGHT - AVATAR_SIZE) / 2;
        ctx.save();
        ctx.beginPath();
        ctx.arc(ax + AVATAR_SIZE / 2, ay + AVATAR_SIZE / 2, AVATAR_SIZE / 2, 0, Math.PI * 2);
        ctx.clip();
        if (node.chargeImage != null && node.chargeImage.complete) {
            ctx.drawImage((ImageElement) (Object) node.chargeImage, ax, ay, AVATAR_SIZE, AVATAR_SIZE);
        } else {
            ctx.setFillStyle("#eee");
            ctx.fill();
        }
        ctx.restore();

        // 文字
        ctx.setFillStyle("#333");
        ctx.setFont("bold 14px Arial");
        ctx.fillText(node.data.getName(), node.x + 55, node.y + 25);
        ctx.setFillStyle("#888");
        ctx.setFont("12px Arial");
        int count = node.data.getMembers() != null ? node.data.getMembers().size() : 0;
        ctx.fillText("成员: " + count, node.x + 55, node.y + 45);

        // 右键高亮
        if (node.data.getId().equals(contextMenuNodeId)) {
            ctx.setStrokeStyle("#ff4d4f");
            ctx.setLineWidth(2);
            drawRoundedRect(ctx, node.x, node.y, NODE_WIDTH, NODE_HEIGHT, CORNER_RADIUS);
            ctx.stroke();
        }
    }

    private void drawRoundedRect(Context2d ctx, double x, double y, double w, double h, double r) {
        ctx.beginPath();
        ctx.moveTo(x + r, y);
        ctx.lineTo(x + w - r, y);
        ctx.quadraticCurveTo(x + w, y, x + w, y + r);
        ctx.lineTo(x + w, y + h - r);
        ctx.quadraticCurveTo(x + w, y + h, x + w - r, y + h);
        ctx.lineTo(x + r, y + h);
        ctx.quadraticCurveTo(x, y + h, x, y + h - r);
        ctx.lineTo(x, y + r);
        ctx.quadraticCurveTo(x, y, x + r, y);
        ctx.closePath();
    }

    private void onNodeClicked(DevProjectTeamEntity team) {
        DomGlobal.console.log("选中: " + team.getName());
    }

    @Override
    public List<DevProjectTeamEntity> getData() {
        return rootTeams;
    }

    @Override
    public void setData(List<DevProjectTeamEntity> obj) {
        this.rootTeams = obj;
        calculateLayout();
        zoomToFit();
    }

}