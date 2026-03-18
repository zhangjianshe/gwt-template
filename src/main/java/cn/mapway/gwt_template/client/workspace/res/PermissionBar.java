package cn.mapway.gwt_template.client.workspace.res;

import cn.mapway.gwt_template.shared.rpc.project.module.ProjectPermission;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectPermissionKind;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.buttons.CheckBoxEx;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.HasCommonHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HorizontalPanel;
import lombok.Setter;

public class PermissionBar extends HorizontalPanel implements HasCommonHandlers, IData<ProjectPermission> {
    @Setter
    boolean enableEdit = false;
    private ProjectPermission permission;
    private final ValueChangeHandler<Boolean> checkBoxExHandler = new ValueChangeHandler<Boolean>() {
        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
            CheckBoxEx source = (CheckBoxEx) event.getSource();
            ProjectPermissionKind kind = (ProjectPermissionKind) source.getData();
            permission.set(kind, event.getValue());
            fireEvent(CommonEvent.updateEvent(permission));
        }
    };

    public PermissionBar() {
        setSpacing(4);
    }

    /**
     * 添加权限按钮 之前必须先设定 setData permission 和 enableEdit
     *
     * @param kind
     */
    public void addPermission(ProjectPermissionKind kind) {
        CheckBoxEx checkBoxEx = new CheckBoxEx();
        checkBoxEx.setEnabled(enableEdit);
        if (enableEdit) {
            checkBoxEx.addValueChangeHandler(checkBoxExHandler);
        }
        checkBoxEx.setData(kind);
        checkBoxEx.setTitle(kind.getLabel());
        add(checkBoxEx);
        checkBoxEx.setValue(permission.has(kind), false);
    }

    @Override
    public HandlerRegistration addCommonHandler(CommonEventHandler handler) {
        return addHandler(handler, CommonEvent.TYPE);
    }

    @Override
    public ProjectPermission getData() {
        return permission;
    }

    @Override
    public void setData(ProjectPermission obj) {
        permission = obj;
        clear();
    }
}
