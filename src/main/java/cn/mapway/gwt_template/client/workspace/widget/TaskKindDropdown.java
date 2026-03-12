package cn.mapway.gwt_template.client.workspace.widget;

import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskKind;
import cn.mapway.ui.client.widget.Dropdown;

public class TaskKindDropdown extends Dropdown {
    public TaskKindDropdown() {
        for (DevTaskKind kind : DevTaskKind.values()) {
            addItem("",kind.getUnicode()+kind.getName(),kind.getCode());
        }
    }
}
