package cn.mapway.gwt_template.client.workspace.wiki.component;

import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.workspace.widget.EditableLabel;
import cn.mapway.gwt_template.shared.db.DevProjectPageEntity;
import cn.mapway.gwt_template.shared.db.DevProjectPageSectionEntity;
import cn.mapway.gwt_template.shared.doc.PageMetadata;
import cn.mapway.gwt_template.shared.wiki.component.WikiBaseComponent;
import cn.mapway.gwt_template.shared.wiki.component.WikiComponent;
import cn.mapway.gwt_template.shared.wiki.component.WikiPageContext;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.tools.JSON;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

@WikiComponent(
        kind = PageTitleComponent.KIND_PAGE,
        name = "页面头",
        unicode = Fonts.FILE,
        summary = "页面头",
        catalog = "系统"
)
public class PageTitleComponent extends WikiBaseComponent {
    public static final String KIND_PAGE = "page";
    private static final PageTitleComponentUiBinder ourUiBinder = GWT.create(PageTitleComponentUiBinder.class);
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

    public PageTitleComponent() {
        initWidget(ourUiBinder.createAndBindUi(this));
        txtHeader.addValueChangeHandler(event -> {
            DevProjectPageEntity page = getContext().getPage();
            if (txtHeader.getValue().equals(page.getName())) {
                return;
            }
            metadata.title = txtHeader.getValue();
            page.setName(metadata.title);
            DevProjectPageSectionEntity section = getSection();
            section.setContent(JSON.stringify(metadata));
            fireEvent(CommonEvent.updateEvent(section));
        });
    }

    @Override
    public void initComponent(WikiPageContext context, DevProjectPageSectionEntity section) {
        super.initComponent(context, section);
        DevProjectPageEntity page = context.getPage();
        txtHeader.setValue(page.getName());
        if (StringUtil.isNotBlank(page.getUserAvatar())) {
            lbAvatar.setUrl(page.getUserAvatar());
        } else {
            lbAvatar.setResource(AppResource.INSTANCE.emptyAvatar());
        }
        lbUserName.setText(page.getUserName());
        lbVersion.setText(StringUtil.brief(page.getLastCommit(), 10));
        lbModify.setText(StringUtil.formatDate(page.getCreateTime()));
    }

    @Override
    public Widget getRootWidget() {
        return this;
    }

    @Override
    public void focus() {
        txtHeader.getElement().focus();
    }

    interface PageTitleComponentUiBinder extends UiBinder<HTMLPanel, PageTitleComponent> {
    }
}