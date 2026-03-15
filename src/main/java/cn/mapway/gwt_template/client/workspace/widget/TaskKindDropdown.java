package cn.mapway.gwt_template.client.workspace.widget;

import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskKind;

public class TaskKindDropdown extends SingleCheck {
    public TaskKindDropdown() {
        for (DevTaskKind kind : DevTaskKind.values()) {
            addItem("<span style='font-size:32px;color:" + kind.getColor() + "'>" + kind.getUnicode() + "</span><div style='font-size:0.8rem;white-space:nowrap;'>" + kind.getName() + "</div>", kind.getCode());
        }
    }
}
