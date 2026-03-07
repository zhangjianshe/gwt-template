package cn.mapway.gwt_template.client.workspace;

import cn.mapway.gwt_template.shared.rpc.project.module.ProjectMember;

public class TeamHitResult {
        public TeamHitTest area = TeamHitTest.NONE;
        public TeamGroupNode node = null;

        // 针对成员区域的附加信息
        public int memberIndex = -1;
        public ProjectMember member = null; // 直接持有成员引用，方便直接使用

        // 辅助判定：是否击中了标题栏（通常用于触发拖拽，而内容区可能用于触发选择）
        public boolean isHeader = false;
}
