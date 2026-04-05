package cn.mapway.gwt_template.client.widget;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.shared.rpc.tools.QueryImagesRequest;
import cn.mapway.gwt_template.shared.rpc.tools.QueryImagesResponse;
import cn.mapway.gwt_template.shared.rpc.tools.SysImage;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;

public class IconSelector extends CommonEventComposite {
    private static final IconSelectorUiBinder ourUiBinder = GWT.create(IconSelectorUiBinder.class);
    private static Popup<IconSelector> popup;
    @UiField
    FlowPanel container;
    boolean hasInitialize = false;

    public IconSelector() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Popup<IconSelector> getPopup(boolean reuse) {
        if (reuse) {
            if (popup == null) {
                popup = createOne();
            }
            return popup;
        } else {
            return createOne();
        }
    }

    private static Popup<IconSelector> createOne() {
        IconSelector iconSelector = new IconSelector();
        return new Popup<>(iconSelector);
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(350, 450);
    }

    public void init() {
        if (!hasInitialize) {
            hasInitialize = true;
            AppProxy.get().queryImages(new QueryImagesRequest(), new AsyncAdaptor<RpcResult<QueryImagesResponse>>() {
                @Override
                public void onData(RpcResult<QueryImagesResponse> result) {
                    renderAll(result.getData());
                }
            });
        }
    }

    private void renderAll(QueryImagesResponse data) {
        container.clear();
        for (SysImage image : data.getImages()) {
            IconGroup iconGroup = new IconGroup();
            iconGroup.setData(image);
            container.add(iconGroup);
            iconGroup.addCommonHandler(new CommonEventHandler() {
                @Override
                public void onCommonEvent(CommonEvent event) {
                    if (event.isSelect()) {
                        String url = event.getValue();
                        fireEvent(CommonEvent.selectEvent(url));
                    }
                }
            });
        }
    }

    interface IconSelectorUiBinder extends UiBinder<DockLayoutPanel, IconSelector> {
    }
}