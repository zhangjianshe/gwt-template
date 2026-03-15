package cn.mapway.gwt_template.client.workspace;

import cn.mapway.gwt_template.shared.db.DevWorkspaceFolderEntity;
import cn.mapway.ui.client.widget.Dropdown;

import java.util.List;

public class WorkspaceFolderDropdown extends Dropdown {
    public void init(List<DevWorkspaceFolderEntity> folders) {
        clear();
        if (folders == null || folders.isEmpty()) {
            return;
        }
        for (DevWorkspaceFolderEntity folder : folders) {
            addItem("", folder.getName(), folder.getId());
        }
    }
}
