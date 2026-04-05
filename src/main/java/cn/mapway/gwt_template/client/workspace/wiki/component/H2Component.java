package cn.mapway.gwt_template.client.workspace.wiki.component;

import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.shared.wiki.component.WikiComponent;
import cn.mapway.ui.client.fonts.Fonts;

@WikiComponent(
        kind = H2Component.KIND_H2,
        name = "标题2",
        unicode = Fonts.H2,
        summary = "标题2",
        catalog = "系统",
        alias = "h2"
)
public class H2Component extends HeaderComponent{
    public static final String KIND_H2 = "h2";
    public H2Component() {
        super();
        getEditor().addStyleName(AppResource.INSTANCE.styles().h2());
    }
}
