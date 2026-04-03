package cn.mapway.gwt_template.shared.wiki.component;

import cn.mapway.gwt_template.shared.db.DevProjectPageSectionEntity;
import cn.mapway.ui.client.widget.CommonEventComposite;
import com.google.gwt.dom.client.Element;
import elemental2.dom.HTMLElement;
import jsinterop.base.Js;
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

    public void setPlaceholder(String text) {

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

    public static void setElementEditable(Element element, boolean editable) {
        HTMLElement ele = Js.uncheckedCast(element);
        if (editable) {
            ele.setAttribute("contentEditable", "true");
        } else {
            ele.removeAttribute("contentEditable");
        }
    }
}
