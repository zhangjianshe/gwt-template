package cn.mapway.gwt_template.client.workspace.task;

import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectMember;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import lombok.Getter;

/**
 * 项目成员选择
 */
public class ProjectMemberWidget extends CommonEventComposite {
    private static final ProjectMemberWidgetUiBinder ourUiBinder = GWT.create(ProjectMemberWidgetUiBinder.class);
    @UiField
    Label lbName;
    @Getter
    @UiField
    Image icon;
    Long userId;
    String projectId;

    public ProjectMemberWidget() {
        initWidget(ourUiBinder.createAndBindUi(this));
        icon.addErrorHandler(event -> icon.setUrl(AppResource.INSTANCE.emptyAvatar().getSafeUri()));
        icon.addClickHandler(event -> {
            showSelectorDialog(projectId);
        });
    }

    public void setImageWidth(int width) {
        icon.setWidth(width + "px");
    }

    private void showSelectorDialog(String projectId) {
        Popup<ProjectMemberSelector> popup = ProjectMemberSelector.getPopup(true);
        popup.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isSelect()) {
                    ProjectMember member = event.getValue();
                    lbName.setText(member.getUserName());
                    icon.setUrl(member.getAvatar());
                    userId = member.getUserId();
                    fireEvent(CommonEvent.selectEvent(member));
                }
                popup.hide();
            }
        });
        popup.getContent().setData(projectId);
        popup.showRelativeTo(this);

    }

    public void setData(String projectId, Long userId, String name, String avatar) {
        this.projectId = projectId;
        this.userId = userId;
        lbName.setText(name);
        icon.setUrl(avatar);
    }

    interface ProjectMemberWidgetUiBinder extends UiBinder<HTMLPanel, ProjectMemberWidget> {
    }
}