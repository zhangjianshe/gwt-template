package cn.mapway.gwt_template.client.workspace.widget;

import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskStatus;

public class TaskStatusDropdown extends SingleCheck {
    public TaskStatusDropdown() {
        for (DevTaskStatus kind : DevTaskStatus.values()) {
            addItem(kind.getName(), kind.getCode());
        }
    }
}
