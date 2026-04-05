package cn.mapway.gwt_template.client.workspace.wiki.component;

import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.shared.wiki.component.WikiComponent;
import cn.mapway.ui.client.fonts.Fonts;

@WikiComponent(
        kind = H1Component.KIND_H1,
        name = "标题1",
        unicode = Fonts.H1,
        summary = "标题1",
        catalog = "系统",
        alias = "h1"
)
public class H1Component extends HeaderComponent{
    public static final String KIND_H1 = "h1";
    public H1Component() {
        super();
        getEditor().addStyleName(AppResource.INSTANCE.styles().h1());
    }
}
