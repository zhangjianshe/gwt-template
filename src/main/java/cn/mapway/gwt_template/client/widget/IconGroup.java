package cn.mapway.gwt_template.client.widget;

import cn.mapway.gwt_template.shared.rpc.tools.SysImage;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

import java.util.List;

public class IconGroup extends CommonEventComposite implements IData<SysImage> {
    private static final IconGroupUiBinder ourUiBinder = GWT.create(IconGroupUiBinder.class);
    @UiField
    HTMLPanel list;
    @UiField
    SStyle style;
    @UiField
    Label lbCatalog;
    private SysImage sysImage;

    public IconGroup() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public SysImage getData() {
        return sysImage;
    }

    @Override
    public void setData(SysImage obj) {
        sysImage = obj;
        toUI();
    }

    private void toUI() {
        lbCatalog.setText(sysImage.getCatalog());
        list.clear();
        List<String> images = sysImage.getImages();
        for (int i = 0; i < images.size(); i++) {
            String url = images.get(i);
            Image image = new Image(url);
            image.setStyleName(style.icon());
            list.add(image);
            image.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    fireEvent(CommonEvent.selectEvent(url));
                }
            });
        }
    }
    interface IconGroupUiBinder extends UiBinder<HTMLPanel, IconGroup> {
    }

    interface SStyle extends CssResource {

        String catalog();

        String icon();

        String box();

        String list();
    }
}