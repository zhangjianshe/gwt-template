package cn.mapway.gwt_template.client.workspace.widget;

import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskPriority;

public class TaskPriorityDropdown extends SingleCheck {
    public TaskPriorityDropdown() {
        for (DevTaskPriority kind : DevTaskPriority.values()) {
            addItem(kind.getName(), kind.getCode());
        }
    }
}
