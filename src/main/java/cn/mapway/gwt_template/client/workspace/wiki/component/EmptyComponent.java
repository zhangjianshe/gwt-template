package cn.mapway.gwt_template.client.workspace.wiki.component;

import cn.mapway.gwt_template.shared.wiki.component.WikiBaseComponent;
import cn.mapway.gwt_template.shared.wiki.component.WikiComponent;
import cn.mapway.ui.client.fonts.Fonts;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

@WikiComponent(
        kind = "empty",
        name = "未知组件",
        unicode = Fonts.UNKNOWN,
        summary = "当组件类型无法识别时显示的占位符",
        catalog = "系统",
        alias = ""
)
public class EmptyComponent extends WikiBaseComponent {

    private final HTML widget;

    public EmptyComponent() {
        widget = new HTML();
        widget.setStyleName("wiki-component-empty");
        widget.setHTML("<div style='padding:20px; border:1px dashed #ccc; color:#999; text-align:center;'>"
                + "未识别的组件类型"
                + "</div>");
    }

    @Override
    public Widget getRootWidget() {
        return widget;
    }

    @Override
    public void focus() {

    }
}