package cn.mapway.gwt_template.client.workspace.widget;

import cn.mapway.gwt_template.shared.rpc.file.SecurityLevel;
import cn.mapway.ui.client.widget.Dropdown;

public class SecurityLevelDropdown extends Dropdown {
    public SecurityLevelDropdown() {
        for (SecurityLevel securityLevel : SecurityLevel.values()) {
            addItem("", securityLevel.getText(), securityLevel.getRank());
        }
    }
}
