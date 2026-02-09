package cn.mapway.gwt_template.client.project.webhook;

import cn.mapway.ui.client.widget.Dropdown;

public class MethodDropdown extends Dropdown {
    public MethodDropdown() {
        for (HttpMethod httpMethod : HttpMethod.values()) {
            addItem("", httpMethod.name(), httpMethod);
        }
        setSelectedIndex(0);
    }
}
