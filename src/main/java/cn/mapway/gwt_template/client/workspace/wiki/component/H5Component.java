package cn.mapway.gwt_template.client.workspace.wiki.component;

import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.shared.wiki.component.WikiComponent;
import cn.mapway.ui.client.fonts.Fonts;

@WikiComponent(
        kind = H5Component.KIND_H5,
        name = "标题5",
        unicode = Fonts.H5,
        summary = "标题5",
        catalog = "系统",
        alias = "h5"
)
public class H5Component extends HeaderComponent {
    public static final String KIND_H5 = "h5";

    public H5Component() {
        super();
        getEditor().addStyleName(AppResource.INSTANCE.styles().h5());
    }
}
