package cn.mapway.gwt_template.client.workspace.team;

public enum TeamHitTest {
    NONE,           // 空白处
    AREA_BODY,      // 节点主体（拖拽区）
    AREA_BTN_DROPDOWN,  // 展开/收缩按钮
    AREA_MEMBER_ITEM,     // 具体的某个成员
    AREA_CHARGE      // 负责人头像区域
}
