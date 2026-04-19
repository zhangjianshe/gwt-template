package cn.mapway.gwt_template.client.desktop;

import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class ProjectItem extends Composite implements IData<DevProjectEntity> {
    private static final ProjectItemUiBinder ourUiBinder = GWT.create(ProjectItemUiBinder.class);
    DevProjectEntity project;
    @UiField
    Label lbDate;
    @UiField
    Label lbUserName;
    @UiField
    Image icon;
    @UiField
    Label lbName;

    public ProjectItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
        icon.addErrorHandler(new ErrorHandler() {
            @Override
            public void onError(ErrorEvent event) {
                icon.setResource(AppResource.INSTANCE.avatar());
            }
        });
    }

    @Override
    public DevProjectEntity getData() {
        return project;
    }

    @Override
    public void setData(DevProjectEntity obj) {
        project = obj;
        toUI();
    }

    private void toUI() {
        lbName.setText(project.getName());
        lbName.getElement().getStyle().setColor(project.getColor());
        lbUserName.setText(project.getCreateUserName());
        if (StringUtil.isBlank(project.getCreateUserAvatar())) {
            icon.setResource(AppResource.INSTANCE.emptyAvatar());
        } else {
            icon.setUrl(project.getCreateUserAvatar());
        }
        lbDate.setText(StringUtil.formatDate(project.getCreateTime(), "yyyy-MM-dd"));
    }

    interface ProjectItemUiBinder extends UiBinder<HTMLPanel, ProjectItem> {
    }
}