package cn.mapway.gwt_template.client.dashboard;

import cn.mapway.gwt_template.client.widget.IWidgetConfig;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.BaseAbstractModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTMLPanel;
import elemental2.promise.Promise;
import jsinterop.base.Js;

import static cn.mapway.gwt_template.client.dashboard.IFrameWidget.MODULE_CODE;


@ModuleMarker(
        name = "页面组件",
        value = MODULE_CODE,
        unicode = Fonts.PAGE,
        summary = "我的快捷方式",
        tags = {AppConstant.TAG_WIDGET}
)
public class IFrameWidget extends BaseAbstractModule implements IWidgetConfig {
    public final static String MODULE_CODE = "widget_iframe";
    private static final IFrameWidgetUiBinder ourUiBinder = GWT.create(IFrameWidgetUiBinder.class);
    @UiField
    Frame frame;

    public IFrameWidget() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public Promise<Object> showConfig(Object parameter) {
        return new Promise<Object>(new Promise.PromiseExecutorCallbackFn<Object>() {
            @Override
            public void onInvoke(ResolveCallbackFn<Object> resolve, RejectCallbackFn reject) {
                Dialog<IFrameConfigDialog> dialog = IFrameConfigDialog.getDialog(true);
                dialog.addCommonHandler(new CommonEventHandler() {
                    @Override
                    public void onCommonEvent(CommonEvent event) {
                        if (event.isOk()) {
                            IFrameConfig config = Js.uncheckedCast(event.getValue());
                            dialog.hide();
                            resolve.onInvoke(config);
                        } else if (event.isClose()) {
                            dialog.hide();
                            reject.onInvoke("");
                        }
                    }
                });
                dialog.getContent().setData(Js.uncheckedCast(parameter));
                dialog.center();
            }
        });
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        boolean b = super.initialize(parentModule, parameter);
        Object obj = parameter.get();
        if (obj == null || Js.typeof(obj) == "string") {
            setInformation("需要配置参数");
        } else {
            IFrameConfig config = Js.uncheckedCast(obj);
            if (StringUtil.isNotBlank(config.url)) {
                frame.setUrl(config.url);
                fireEvent(CommonEvent.titleEvent(config.title));
            } else {
                setInformation("需要配置参数");
            }
        }
        return b;
    }

    void setInformation(String info) {
        // Style it slightly so it looks clean inside the frame
        String htmlContent = "<div style='font-family:sans-serif; padding:20px; color:#666;'>"
                + info
                + "</div>";

        // Set the srcdoc attribute directly on the underlying iframe element
        frame.getElement().setAttribute("srcdoc", htmlContent);
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    interface IFrameWidgetUiBinder extends UiBinder<HTMLPanel, IFrameWidget> {
    }
}