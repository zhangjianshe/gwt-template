package cn.mapway.gwt_template.client.workspace.wiki;

import cn.mapway.gwt_template.shared.wiki.component.WikiComponentInformation;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

public class WikiComponentItem extends CommonEventComposite implements IData<WikiComponentInformation> {
    private static final WikiCOmponentItemUiBinder ourUiBinder = GWT.create(WikiCOmponentItemUiBinder.class);
    @UiField
    Label lbName;
    @UiField
    FontIcon icon;
    private WikiComponentInformation info;

    public WikiComponentItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public WikiComponentInformation getData() {
        return info;
    }

    @Override
    public void setData(WikiComponentInformation wikiComponentInformation) {
        info = wikiComponentInformation;
        toUI();
    }

    private void toUI() {
        icon.setIconUnicode(info.getUnicode());
        lbName.setText(info.getName());
        lbName.setTitle(info.getSummary());
    }

    interface WikiCOmponentItemUiBinder extends UiBinder<HTMLPanel, WikiComponentItem> {
    }
}