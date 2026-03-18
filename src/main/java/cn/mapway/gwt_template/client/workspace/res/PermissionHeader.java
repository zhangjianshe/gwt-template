package cn.mapway.gwt_template.client.workspace.res;

import cn.mapway.gwt_template.shared.rpc.project.module.ProjectPermissionKind;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class PermissionHeader extends Composite {
    private static final PermissionHeaderUiBinder ourUiBinder = GWT.create(PermissionHeaderUiBinder.class);
    @UiField
    SStyle style;
    @UiField
    HorizontalPanel root;

    public PermissionHeader() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public void addPermission(ProjectPermissionKind kind) {
        Label label = new Label();
        label.setStyleName(style.item());
        label.setText(kind.getLabel());
        root.add(label);
    }

    interface PermissionHeaderUiBinder extends UiBinder<HorizontalPanel, PermissionHeader> {
    }

    interface SStyle extends CssResource {

        String item();
    }
}