package cn.mapway.gwt_template.client.workspace.widget;

import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskPriority;

public class TaskPriorityDropdown extends SingleCheck {
    public TaskPriorityDropdown() {
        init(false);
    }

    public void init(boolean showNone) {
        clear();
        for (DevTaskPriority kind : DevTaskPriority.values()) {
            if (!showNone && kind.getIsNone()) {
                continue;
            }
            addItem(kind.getName(), kind.getCode());
        }
        if (showNone) {
            setValue(DevTaskPriority.NONE.getCode(), false);
        } else {
            setValue(DevTaskPriority.MEDIUM.getCode(), false);
        }
    }
}
