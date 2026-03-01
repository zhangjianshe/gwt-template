package cn.mapway.gwt_template.client.ldap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public class AddEntryDialog extends Composite {
    interface AddEntryDialogUiBinder extends UiBinder<DockLayoutPanel, AddEntryDialog> {
    }

    private static AddEntryDialogUiBinder ourUiBinder = GWT.create(AddEntryDialogUiBinder.class);

    public AddEntryDialog() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }
}