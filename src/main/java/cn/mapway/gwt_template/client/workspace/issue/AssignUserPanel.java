package cn.mapway.gwt_template.client.workspace.issue;

import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.workspace.task.ProjectMemberSelector;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectMember;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import lombok.Setter;

/**
 * 分配用户组件
 */
public class AssignUserPanel extends CommonEventComposite {
    private static final AssignUserPanelUiBinder ourUiBinder = GWT.create(AssignUserPanelUiBinder.class);
    @UiField
    HTMLPanel root;
    @UiField
    Image imageSource;
    @UiField
    Image imageArrow;
    @UiField
    Image imageTarget;

    @Setter
    String projectId;


    public AssignUserPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
        imageArrow.setResource(AppResource.INSTANCE.rightArrow());
        imageSource.addErrorHandler(event -> imageSource.setUrl(AppResource.INSTANCE.avatar().getSafeUri()));
        imageTarget.addErrorHandler(event -> imageTarget.setUrl(AppResource.INSTANCE.avatar().getSafeUri()));
        addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (getEnabled() && StringUtil.isNotBlank(projectId)) {
                    selectUser();
                }
            }
        }, ClickEvent.getType());
    }

    private void selectUser() {
        Popup<ProjectMemberSelector> popup = ProjectMemberSelector.getPopup(true);
        popup.addCommonHandler(commonEvent -> {
            if (commonEvent.isSelect()) {
                // ProjectMember
                ProjectMember member = commonEvent.getValue();
                setTargetUrl(member.getAvatar());
                fireEvent(CommonEvent.selectEvent(commonEvent.getValue()));

            }
            popup.hide();
        });
        popup.getContent().setData(projectId);
        popup.showRelativeTo(this);
    }

    public void setSourceUrl(String url) {
        if (StringUtil.isNotBlank(url)) {
            imageSource.setUrl(url);
        } else {
            imageSource.setUrl(AppResource.INSTANCE.noData().getSafeUri());
        }
    }

    public void setTargetUrl(String url) {
        if (StringUtil.isNotBlank(url)) {
            imageTarget.setUrl(url);
        } else {
            imageTarget.setUrl(AppResource.INSTANCE.noData().getSafeUri());
        }
    }

    public void setAvatar(String sourceUrl, String targetUrl) {
        setSourceUrl(sourceUrl);
        setTargetUrl(targetUrl);
    }

    interface AssignUserPanelUiBinder extends UiBinder<HTMLPanel, AssignUserPanel> {
    }
}