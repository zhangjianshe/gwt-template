package cn.mapway.gwt_template.client.workspace.wiki.component;

import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.shared.wiki.component.WikiComponent;
import cn.mapway.ui.client.fonts.Fonts;

@WikiComponent(
        kind = H4Component.KIND_H4,
        name = "标题4",
        unicode = Fonts.H4,
        summary = "标题4",
        catalog = "系统",
        alias = "h4"
)
public class H4Component extends HeaderComponent{
    public static final String KIND_H4 = "h4";
    public H4Component() {
        super();
        getEditor().addStyleName(AppResource.INSTANCE.styles().h4());
    }
}
