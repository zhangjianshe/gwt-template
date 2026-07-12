package cn.mapway.gwt_template.client.workspace.project;

import cn.mapway.gwt_template.client.resource.AppResource;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class MemberWidget extends Composite {
    private static final MemberWidgetUiBinder ourUiBinder = GWT.create(MemberWidgetUiBinder.class);
    @UiField
    Label lbName;
    @UiField
    Image avatar;
    public MemberWidget() {
        initWidget(ourUiBinder.createAndBindUi(this));
        avatar.addErrorHandler(new ErrorHandler() {
            @Override
            public void onError(ErrorEvent event) {
                avatar.setResource(AppResource.INSTANCE.emptyAvatar());
            }
        });
    }

    public void setData(String name, String url) {
        avatar.setUrl(url);
        lbName.setText(name);
    }

    interface MemberWidgetUiBinder extends UiBinder<HTMLPanel, MemberWidget> {
    }
}