package cn.mapway.gwt_template.shared.wiki.component;

import cn.mapway.gwt_template.shared.db.DevProjectPageSectionEntity;
import cn.mapway.ui.client.widget.CommonEventComposite;
import lombok.Setter;

public abstract class WikiBaseComponent extends CommonEventComposite implements IWikiComponent {
    WikiPageContext context;
    DevProjectPageSectionEntity section;
    @Setter
    boolean changed = false;

    @Override
    public boolean isChanged() {
        return changed;
    }


    @Override
    public void initComponent(WikiPageContext context, DevProjectPageSectionEntity section) {
        this.context = context;
        this.section = section;
    }

    public WikiPageContext getContext() {
        return context;
    }

    public DevProjectPageSectionEntity getSection() {
        return section;
    }

    @Override
    public void focus() {
    }
}
