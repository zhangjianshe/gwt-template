package cn.mapway.gwt_template.client.workspace.wiki;

import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.workspace.widget.EditableLabel;
import cn.mapway.gwt_template.shared.db.DevProjectPageEntity;
import cn.mapway.gwt_template.shared.db.DevProjectPageSectionEntity;
import cn.mapway.gwt_template.shared.doc.PageMetadata;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.tools.JSON;
import cn.mapway.ui.client.util.StringUtil;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import jsinterop.base.Js;
import lombok.Getter;
import lombok.Setter;

public class PageHeaderItem extends Composite implements IData<DevProjectPageSectionEntity> {
    private static final PageHeaderItemUiBinder ourUiBinder = GWT.create(PageHeaderItemUiBinder.class);
    @Getter
    @Setter
    DevProjectPageEntity page;
    @UiField
    Label lbUserName;
    @UiField
    Label lbVersion;
    @UiField
    Label lbModify;
    @UiField
    HTMLPanel header;
    @UiField
    EditableLabel txtHeader;
    @UiField
    Image lbAvatar;
    private DevProjectPageSectionEntity section;

    public PageHeaderItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public DevProjectPageSectionEntity getData() {
        return section;
    }

    @Override
    public void setData(DevProjectPageSectionEntity devProjectPageSectionEntity) {
        section = devProjectPageSectionEntity;
        toUI();
    }

    private void toUI() {
        PageMetadata metadata = Js.uncheckedCast(JSON.parse(section.getContent()));
        lbUserName.setText(page.getUserName());
        lbVersion.setText(StringUtil.isBlank(page.getLastCommit()) ? "" : StringUtil.brief(page.getLastCommit(), 6));
        lbModify.setText(StringUtil.formatDate(page.getCreateTime()));
        txtHeader.setEditable(true);
        txtHeader.setValue(page.getName());
        if (StringUtil.isNotBlank(page.getUserAvatar())) {
            lbAvatar.setUrl(page.getUserAvatar());
        } else {
            lbAvatar.setResource(AppResource.INSTANCE.emptyAvatar());
        }
    }

    interface PageHeaderItemUiBinder extends UiBinder<HTMLPanel, PageHeaderItem> {
    }
}