package cn.mapway.gwt_template.client.workspace.project;

import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class ProjectTemplateItem extends CommonEventComposite implements IData<DevProjectEntity> {
    private static final ProjectTemplateItemUiBinder ourUiBinder = GWT.create(ProjectTemplateItemUiBinder.class);
    DevProjectEntity project;
    @UiField
    Label lbName;
    @UiField
    Image icon;

    public ProjectTemplateItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
        icon.addErrorHandler(event -> icon.setUrl(AppResource.INSTANCE.noFindProject().getSafeUri().asString()));
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
        if (project != null) {
            if (!StringUtil.isBlank(project.getIcon())) {
                icon.setUrl(project.getIcon());
            } else {
                icon.setUrl("img/default_project.png");
            }
            lbName.setText(project.getName());
        } else {
            icon.setUrl("img/blank_project.png");
            lbName.setText("空白项目");
        }
    }

    interface ProjectTemplateItemUiBinder extends UiBinder<HTMLPanel, ProjectTemplateItem> {
    }
}