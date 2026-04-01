package cn.mapway.gwt_template.client.workspace.wiki;

import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.workspace.widget.EditableLabel;
import cn.mapway.gwt_template.shared.db.DevProjectPageEntity;
import cn.mapway.gwt_template.shared.db.DevProjectPageSectionEntity;
import cn.mapway.gwt_template.shared.doc.PageMetadata;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.tools.JSON;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import jsinterop.base.Js;
import lombok.Getter;
import lombok.Setter;

public class PageHeaderItem extends CommonEventComposite implements IData<DevProjectPageSectionEntity>, IItem {
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
    PageMetadata metadata;
    boolean changed = false;
    private DevProjectPageSectionEntity section;

    public PageHeaderItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
        txtHeader.addValueChangeHandler(event -> {
            if (txtHeader.getValue().equals(page.getName())) {
                return;
            }
            metadata.title = txtHeader.getValue();
            page.setName(metadata.title);
            section.setContent(JSON.stringify(metadata));
            fireEvent(CommonEvent.updateEvent(section));
        });
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
        metadata = Js.uncheckedCast(JSON.parse(section.getContent()));
        lbUserName.setText("作者　" + page.getUserName());
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

    @Override
    public boolean isChanged() {
        return changed;
    }

    interface PageHeaderItemUiBinder extends UiBinder<HTMLPanel, PageHeaderItem> {
    }
}