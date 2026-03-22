package cn.mapway.gwt_template.client.workspace.task;

import cn.mapway.gwt_template.shared.rpc.project.module.ProjectMember;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

/**
 * 项目成员
 */
public class ProjectMemberItem extends CommonEventComposite implements IData<ProjectMember> {
    private static final ProjectMemberItemUiBinder ourUiBinder = GWT.create(ProjectMemberItemUiBinder.class);
    @UiField
    Image icon;
    @UiField
    Label lbName;
    @UiField
    Label lbGroup;

    public ProjectMemberItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

        ProjectMember member;
    @Override
    public ProjectMember getData() {
        return member;
    }

    @Override
    public void setData(ProjectMember obj) {
        member=obj;
        toUI();
    }

    private void toUI() {
        String name=member.getUserName();
        if(StringUtil.isNotBlank(member.getNickName()))
        {
            name=name+"("+member.getNickName()+")";
        }
        lbName.setText(name);
        icon.setUrl(member.getAvatar());
        lbGroup.setText(member.getTeamName());
    }

    interface ProjectMemberItemUiBinder extends UiBinder<HTMLPanel, ProjectMemberItem> {
    }
}