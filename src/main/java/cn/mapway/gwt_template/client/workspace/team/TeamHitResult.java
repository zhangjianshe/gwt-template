package cn.mapway.gwt_template.client.workspace.team;

import cn.mapway.gwt_template.shared.rpc.project.module.ProjectMember;

public class TeamHitResult {
    public TeamHitTest hitArea = TeamHitTest.NONE;
    public TeamGroupNode sourceNode = null;
    public ProjectMember sourceMember = null; // 直接持有成员引用，方便直接使用
    public int sourceMemberIndex = -1;        // 针对成员区域的附加信息

    public void copyFrom(TeamHitResult source) {
        this.hitArea = source.hitArea;
        this.sourceNode = source.sourceNode;
        this.sourceMember = source.sourceMember;
        this.sourceMemberIndex = source.sourceMemberIndex;
    }

    public void clear() {
        hitArea = TeamHitTest.NONE;
        sourceNode = null;
        sourceMember = null;
        sourceMemberIndex = -1;
    }

    public boolean isClear() {
        return hitArea == TeamHitTest.NONE;
    }

    public boolean isSameNode(TeamHitResult hitCurrent) {
        if(hitCurrent==null || hitCurrent.sourceNode==null || sourceNode==null ) return false;
        return hitCurrent.sourceNode.getData().getId().equals(sourceNode.getData().getId());
    }
}
