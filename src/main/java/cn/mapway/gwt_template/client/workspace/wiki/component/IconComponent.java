package cn.mapway.gwt_template.client.workspace.wiki.component;

import cn.mapway.gwt_template.client.widget.IconSelector;
import cn.mapway.gwt_template.shared.db.DevProjectPageSectionEntity;
import cn.mapway.gwt_template.shared.wiki.component.WikiBaseComponent;
import cn.mapway.gwt_template.shared.wiki.component.WikiComponent;
import cn.mapway.gwt_template.shared.wiki.component.WikiPageContext;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import static cn.mapway.gwt_template.client.workspace.wiki.component.IconComponent.KIND_ICON;

/**
 * content about icon will be an xml
 * <img src=''>
 */
@WikiComponent(
        kind = KIND_ICON,
        name = "表情和图标",
        unicode = Fonts.IMAGE,
        summary = "表情和图标",
        catalog = "系统",
        alias = "img"
)
public class IconComponent extends WikiBaseComponent {
    public final static String KIND_ICON = "icon";
    Image image;

    public IconComponent() {
        super();
        image = new Image();
        initWidget(image);
        image.addClickHandler(event -> {
            changeIcon();
        });
    }

    private void changeIcon() {
        Popup<IconSelector> popup = IconSelector.getPopup(true);
        popup.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isSelect()) {
                    String url = image.getUrl();
                    setChanged(true);
                    if (StringUtil.isNotBlank(url)) {
                        image.setUrl(url);
                    }
                    popup.hide();
                } else if (event.isClose()) {
                    popup.hide();
                }
            }
        });
        popup.showRelativeTo(image);
    }

    @Override
    public void initComponent(WikiPageContext context, DevProjectPageSectionEntity section) {
        super.initComponent(context, section);
    }

    @Override
    public Widget getRootWidget() {
        return this;
    }
}
