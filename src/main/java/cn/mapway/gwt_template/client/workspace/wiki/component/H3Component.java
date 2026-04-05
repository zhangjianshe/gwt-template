package cn.mapway.gwt_template.client.workspace.wiki.component;

import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.shared.wiki.component.WikiComponent;
import cn.mapway.ui.client.fonts.Fonts;

@WikiComponent(
        kind = H3Component.KIND_H3,
        name = "标题3",
        unicode = Fonts.H3,
        summary = "标题3",
        catalog = "系统",
        alias = "h3"
)
public class H3Component extends HeaderComponent{
    public static final String KIND_H3 = "h3";
    public H3Component() {
        super();
        getEditor().addStyleName(AppResource.INSTANCE.styles().h3());
    }
}
