package cn.mapway.gwt_template.client.workspace.team;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DevProjectTeamEntity;
import cn.mapway.gwt_template.shared.rpc.project.*;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectMember;
import cn.mapway.ui.client.IUserInfo;
import cn.mapway.ui.client.mvc.Rect;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.user.client.rpc.AsyncCallback;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLImageElement;
import elemental2.promise.IThenable;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class TeamCanvasData {
    // sourceNode cache
    private final List<TeamGroupNode> flatNodes = new java.util.ArrayList<>();
    private final Map<String, TeamGroupNode> nodeMap = new HashMap<>();
    TeamCanvas teamCanvas;
    Map<String, HTMLImageElement> avatars = new HashMap<>();
    Rect contentBounds = new Rect();
    String currentProjectId;
    private List<TeamGroupNode> rootNodes = new java.util.ArrayList<>();
    // source of truth
    private List<DevProjectTeamEntity> rootTeams;

    public TeamCanvasData(TeamCanvas canvas) {
        this.teamCanvas = canvas;
    }

    public void rebuild(String projectId) {
        currentProjectId = projectId;
        QueryProjectTeamRequest request = new QueryProjectTeamRequest();
        request.setProjectId(projectId);
        AppProxy.get().queryProjectTeam(request, new AsyncCallback<RpcResult<QueryProjectTeamResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryProjectTeamResponse> result) {
                if (result.isSuccess()) {
                    CommonPermission permission = CommonPermission.from(result.getData().getPermission());
                    teamCanvas.setReadonly(!permission.isAdmin());
                    setData(result.getData().getRootTeams());
                }
            }
        });

    }

    private void setData(List<DevProjectTeamEntity> teams) {
        this.rootTeams = teams;
        this.flatNodes.clear();
        this.nodeMap.clear();
        this.rootNodes.clear();

        // 第一步：构建纯粹的 Node 树
        this.rootNodes = buildTree(rootTeams, null);

        // 第二步：物理布局计算（填充每个 sourceNode.rect）
        calculateLayout(null);
        teamCanvas.zoomToFit();
        teamCanvas.redraw();
    }

    private List<TeamGroupNode> buildTree(List<DevProjectTeamEntity> teams, TeamGroupNode parentNode) {
        List<TeamGroupNode> currentLevelNodes = new ArrayList<>();
        if (teams == null) return currentLevelNodes;

        for (DevProjectTeamEntity team : teams) {
            TeamGroupNode node = new TeamGroupNode();
            node.setData(team);
            node.setParent(parentNode);
            node.getRect().setWidth(180);

            nodeMap.put(team.getId(), node);
            flatNodes.add(node);
            preloadAvatar(node);

            // 递归构建子树
            node.setChildren(buildTree(team.getChildren(), node));
            currentLevelNodes.add(node);
        }
        return currentLevelNodes;
    }

    private void preloadAvatar(TeamGroupNode node) {
        String url = AppResource.INSTANCE.noData().getSafeUri().asString();
        DevProjectTeamEntity teamEntity = node.getData();
        if (teamEntity.getCharger() != null && teamEntity.getMembers() != null) {
            for (ProjectMember m : teamEntity.getMembers()) {
                if (teamEntity.getCharger().equals(m.getUserId())) {
                    url = m.getAvatar();
                    break;
                }
            }
        }
        if (StringUtil.isBlank(url)) {
            url = AppResource.INSTANCE.emptyAvatar().getSafeUri().asString();
        }
        // 简单的缓存判断
        if (node.getChargeImage() != null && url.equals(node.getChargeImage().src)) {
            return;
        }
        HTMLImageElement img = avatars.get(url);
        if (img == null) {
            img = (HTMLImageElement) DomGlobal.document.createElement("img");
            avatars.put(url, img);
            img.src = url;
            img.onload = (e) -> {
                teamCanvas.redraw();
                return null;
            };
        }
        node.setChargeImage(img);
    }

    /**
     * 在逻辑坐标系下寻找潜在的父节点
     */
    public TeamGroupNode findPotentialParent(TeamGroupNode movingNode) {
        // 遍历所有节点，寻找一个“不是我自己”且“包含我左边缘中心点”的节点
        Rect rect = movingNode.getRect();
        Size leftCenter = new Size(rect.x, rect.y + movingNode.getTitleHeight() / 2.0);

        for (TeamGroupNode target : flatNodes) {
            if (target == movingNode) continue;
            // 如果移动节点的左中心进入了目标节点的矩形范围（或者稍微扩大的感应区）
            if (target.getRect().contains(leftCenter.x, leftCenter.y)) {
                return target;
            }
        }
        return null;
    }

    /**
     * 递归检查 potential 是否为 hitArea 的后代
     */
    public boolean isDescendantOf(TeamGroupNode potential, TeamGroupNode target) {
        String parentId = potential.getData().getParentId();
        if (StringUtil.isBlank(parentId)) return false;

        // 如果潜在父节点的父 ID 就是当前拖拽节点，说明它是后代
        if (parentId.equals(target.getData().getId())) return true;

        // 向上追溯
        TeamGroupNode parent = nodeMap.get(parentId);
        if (parent != null) {
            return isDescendantOf(parent, target);
        }
        return false;
    }

    /**
     * 判断指定节点是否为非法放置目标
     */
    public boolean isInvalidDropTarget(TeamGroupNode draggingNode, TeamGroupNode target) {
        if (draggingNode == null) return false;

        // 1. 自身不能作为父节点
        if (target == draggingNode) return true;

        // 2. 当前的父节点（已经是父子关系了，没必要挂载）
        if (target.getData().getId().equals(draggingNode.getData().getParentId())) return true;

        // 3. 自己的子孙节点（最关键：防止循环引用）
        return isDescendantOf(target, draggingNode);
    }

    public void doDeleteTeam(TeamGroupNode nodeToDelete) {
        DeleteProjectTeamRequest request = new DeleteProjectTeamRequest();
        request.setTeamId(nodeToDelete.getData().getId());
        AppProxy.get().deleteProjectTeam(request, new AsyncCallback<RpcResult<DeleteProjectTeamResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<DeleteProjectTeamResponse> result) {
                if (result.isSuccess()) {
                    rebuild(currentProjectId);
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
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

    public void calculateLayout(@Nullable TeamGroupNode anchorNode) {
        if (rootNodes.isEmpty()) return;

        // 1. 记录锚点节点的屏幕位置 (如果传入了锚点)
        double oldLogicY = 0;
        double oldScreenY = 0;
        boolean useAnchor = (anchorNode != null);

        if (useAnchor) {
            // 记录该节点当前的屏幕 Y 坐标
            // ScreenY = translation.y + LogicY * currentScale
            oldLogicY = anchorNode.getRect().y;
            oldScreenY = teamCanvas.translation.y + oldLogicY * teamCanvas.currentScale;
        }

        // 2. 执行原有的重算逻辑
        contentBounds.set(0, 0, 0, 0);
        computeTreeGeometry(rootNodes, 0, 0);
        syncNodes();

        // 3. 补偿位移
        if (useAnchor) {
            // 新的 translation.y = oldScreenY - newLogicY * currentScale
            double newLogicY = anchorNode.getRect().y;
            double newTranslationY = oldScreenY - newLogicY * teamCanvas.currentScale;

            teamCanvas.translation.y = newTranslationY;
        }

        teamCanvas.redraw();
    }

    // 每次重建数据或 RPC 返回时同步
    private void syncNodes() {
        nodeMap.clear();
        for (TeamGroupNode node : flatNodes) {
            nodeMap.put(node.getData().getId(), node);
        }
    }

    private void updateBounds(TeamGroupNode node) {
        // 如果是第一个节点，直接复制，否则扩展
        if (contentBounds.width == 0 && contentBounds.height == 0) {
            contentBounds.copyFrom(node.getRect());
        } else {
            contentBounds.extend(node.getRect());
        }
    }

    private double computeTreeGeometry(List<TeamGroupNode> nodes, double x, double y) {
        double currentLevelY = y;

        for (TeamGroupNode node : nodes) {
            double h = node.getDesiredHeight();
            Rect rect = node.getRect();
            // 先赋予标准位置
            rect.set(x, currentLevelY, node.getWidth(), h);

            List<TeamGroupNode> children = node.getChildren();
            if (children != null && !children.isEmpty()) {
                // 递归子树
                double subTreeTotalHeight = computeTreeGeometry(children, x + node.getWidth() + node.getLevelGap(), currentLevelY);

                // 核心：父节点垂直对齐到子树的几何中心
                double subTreeCenterY = currentLevelY + (subTreeTotalHeight / 2.0);
                rect.y = subTreeCenterY - (rect.height / 2.0);

                // 下一个同级节点的起始 Y：必须跳过整个子树的高度或父节点高度（取大者）
                currentLevelY += Math.max(rect.height, subTreeTotalHeight) + node.getNodeGap();
            } else {
                // 叶子节点，直接累加高度
                currentLevelY += rect.height + node.getNodeGap();
            }

            // 更新全局边界包围盒
            updateBounds(node);
        }
        return currentLevelY - y; // 返回本层级占用的总垂直高度
    }

    public boolean isEmpty() {
        return flatNodes.isEmpty();
    }

    /**
     * 清除除指定节点外所有节点的选中状态
     *
     * @param exceptNode 需要保持选中状态的节点，如果为 null 则清除所有
     */
    public void clearAllSelectionsExcept(@Nullable TeamGroupNode exceptNode) {
        for (TeamGroupNode node : flatNodes) {
            if (node != exceptNode) {
                node.setSelected(false);
            }
        }
    }

    public void clearSelection() {
        for (TeamGroupNode node : flatNodes) {
            node.setSelected(false);
        }
    }

    public boolean hitTest(TeamHitResult result, double lx, double ly, TeamGroupNode excludedNode) {
        // 逆序遍历，确保点击的是最上层的节点（Z-index 优先）
        for (int i = flatNodes.size() - 1; i >= 0; i--) {
            TeamGroupNode node = flatNodes.get(i);
            if (excludedNode != null && node.getData().equals(excludedNode.getData())) {
                continue;
            }
            boolean hit = flatNodes.get(i).hitTest(result, lx, ly);
            if (hit) {
                return true;
            }
        }
        // 如果没有任何节点被命中
        result.clear();
        return false;
    }


    public void doAddTeam(TeamGroupNode parentNode, String teamName) {
        if (StringUtil.isBlank(teamName)) {
            return;
        }

        DevProjectTeamEntity parent = parentNode.getData();
        DevProjectTeamEntity newChild = new DevProjectTeamEntity();
        newChild.setName(teamName);
        newChild.setParentId(parent.getId());
        newChild.setProjectId(parent.getProjectId());
        newChild.setTeamPermission(CommonPermission.empty().toString());
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
                    newNode.setParent(parentNode); // 建立父级引用
                    newNode.setExpanded(true);   // 新节点默认展开（如果它以后有成员）

                    // 2. 数据层同步：将新实体加入父实体的 children
                    if (parentNode.getData().getChildren() == null) {
                        parentNode.getData().setChildren(new java.util.ArrayList<>());
                    }
                    parentNode.getData().getChildren().add(newEntity);

                    // 3. 逻辑层同步：将新包装器加入父包装器的 children
                    if (parentNode.getChildren() == null) {
                        parentNode.setChildren(new ArrayList<>());
                    }
                    parentNode.getChildren().add(newNode);

                    // 4. 索引同步：将新节点加入全局列表和 Map 索引
                    flatNodes.add(newNode);
                    nodeMap.put(newEntity.getId(), newNode); // 如果你使用了 Map 索引

                    // 5. 交互优化：确保父节点处于展开状态，否则看不见新节点
                    parentNode.setExpanded(true);

                    // 6. 重新布局并绘图
                    calculateLayout(null);

                    ClientContext.get().toast(0, 0, "团队 " + newEntity.getName() + " 创建成功");

                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    public void doAddMember(final DevProjectTeamEntity team, @Nullable IUserInfo user) {
        UpdateProjectMemberRequest request = new UpdateProjectMemberRequest();
        request.setProjectId(team.getProjectId());
        request.setSourceTeamId(team.getId());
        request.setUserId(Long.parseLong(user.getId()));
        request.setAction(UpdateProjectMemberRequest.ACTION_ADD);
        request.setSummary("");
        AppProxy.get().updateProjectMember(request, new AsyncCallback<RpcResult<UpdateProjectMemberResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateProjectMemberResponse> result) {
                rebuild(currentProjectId);
            }
        });
    }

    /**
     * 在树形结构中递归查找指定 ID 的实体对象
     *
     * @param targetId 目标 ID
     * @return 找到的实体对象，未找到则返回 null
     */
    public DevProjectTeamEntity findEntityInTree(String targetId) {
        TeamGroupNode teamGroupNode = nodeMap.get(targetId);
        if (teamGroupNode == null) {
            return null;
        }
        return teamGroupNode.getData();
    }

    public void doChangeTeamParent(DevProjectTeamEntity child, DevProjectTeamEntity newParent) {

        UpdateProjectTeamRequest request = new UpdateProjectTeamRequest();
        DevProjectTeamEntity temp = new DevProjectTeamEntity();
        temp.setId(child.getId());
        temp.setProjectId(child.getProjectId());
        temp.setParentId(newParent.getId()); // 修改父 ID
        request.setProjectTeam(temp);

        // 2. 调用 RPC
        AppProxy.get().updateProjectTeam(request, new AsyncCallback<RpcResult<UpdateProjectTeamResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, "移动失败: " + caught.getMessage());
                calculateLayout(null);
            }

            @Override
            public void onSuccess(RpcResult<UpdateProjectTeamResponse> result) {
                if (result.isSuccess()) {
                    // 从原位置移除并加入新位置
                    moveNodeInLocalTree(child.getId(), newParent.getId());
                    calculateLayout(null);
                    ClientContext.get().toast(0, 0, "层级已更新");
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                    calculateLayout(null);
                }
            }
        });
    }

    private void moveNodeInLocalTree(String nodeId, String newParentId) {
        // 1. 同步原始数据实体 (Entity)
        DevProjectTeamEntity movedEntity = findAndRemoveFromTree(rootTeams, nodeId);
        if (movedEntity != null) {
            DevProjectTeamEntity parentEntity = findEntityInTree(newParentId);
            if (parentEntity != null) {
                if (parentEntity.getChildren() == null) {
                    parentEntity.setChildren(new java.util.ArrayList<>());
                }
                parentEntity.getChildren().add(movedEntity);
                movedEntity.setParentId(parentEntity.getId());
            }
        }

        TeamGroupNode childNode = nodeMap.get(nodeId);
        TeamGroupNode newParentNode = nodeMap.get(newParentId);

        if (childNode != null && newParentNode != null) {
            // 解除旧关系
            if (childNode.getParent() != null && childNode.getParent().getChildren() != null) {
                childNode.getParent().getChildren().remove(childNode);
            }
            // 建立新关系
            childNode.setParent(newParentNode);
            if (newParentNode.getChildren() == null) {
                newParentNode.setChildren(new ArrayList<>());
            }
            newParentNode.getChildren().add(childNode);
        }

        // 3. 执行重排与重绘
        calculateLayout(null);
    }

    public void doChangeTeamParent(TeamGroupNode node, TeamGroupNode newParent) {
        doChangeTeamParent(node.getData(), newParent.getData());
    }

    public void changeNodeCharge(TeamHitResult hitOrigin, TeamHitResult hitCurrent) {
        UpdateProjectMemberRequest request = new UpdateProjectMemberRequest();
        request.setProjectId(hitOrigin.sourceNode.getData().getProjectId());
        request.setSourceTeamId(hitCurrent.sourceNode.getData().getId());
        request.setUserId(hitOrigin.sourceMember.getUserId());
        request.setAction(UpdateProjectMemberRequest.ACTION_SET_CHARGER);
        AppProxy.get().updateProjectMember(request, new AsyncCallback<RpcResult<UpdateProjectMemberResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateProjectMemberResponse> result) {
                if (result.isSuccess()) {
                    rebuild(currentProjectId);
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    /**
     * 将成员从一个小组移动到另外一个小组
     *
     * @param hitOrigin
     * @param hitCurrent
     */
    public void moveMember(TeamHitResult hitOrigin, TeamHitResult hitCurrent) {
        if (hitCurrent == null || hitCurrent.isClear()) {
            String msg = "移除项目组成员" + hitOrigin.sourceMember.getUserName() + "?";
            //必须拷贝
            final TeamHitResult tempResult = new TeamHitResult();
            tempResult.copyFrom(hitOrigin);
            ClientContext.get().confirmDelete(msg).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
                @Override
                public @Nullable IThenable<Object> onInvoke(Void p0) {
                    UpdateProjectMemberRequest request = new UpdateProjectMemberRequest();
                    request.setProjectId(tempResult.sourceNode.getData().getProjectId());
                    request.setSourceTeamId(tempResult.sourceNode.getData().getId());
                    request.setUserId(tempResult.sourceMember.getUserId());
                    request.setAction(UpdateProjectMemberRequest.ACTION_REMOVE);
                    updateMember(request);
                    return null;
                }
            });
        } else {
            if (hitOrigin.isSameNode(hitCurrent)) {
                return;
            }
            UpdateProjectMemberRequest request = new UpdateProjectMemberRequest();
            request.setProjectId(hitOrigin.sourceNode.getData().getProjectId());
            request.setSourceTeamId(hitOrigin.sourceNode.getData().getId());
            request.setUserId(hitOrigin.sourceMember.getUserId());
            request.setTargetTeamId(hitCurrent.sourceNode.getData().getId());
            request.setAction(UpdateProjectMemberRequest.ACTION_MOVE);
            updateMember(request);
        }
    }

    private void updateMember(UpdateProjectMemberRequest request) {
        AppProxy.get().updateProjectMember(request, new AsyncCallback<RpcResult<UpdateProjectMemberResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateProjectMemberResponse> result) {
                if (result.isSuccess()) {
                    rebuild(currentProjectId);
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });

    }

    public @Nullable TeamGroupNode getFirstNode() {
        if (rootNodes != null && !rootNodes.isEmpty()) {
            return rootNodes.get(0);
        }
        return null;
    }
}
