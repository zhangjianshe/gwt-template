package cn.mapway.gwt_template.client.dashboard;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.widget.IWidgetConfig;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.BaseAbstractModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.mvc.attribute.DataCastor;
import cn.mapway.ui.client.util.StringUtil;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTMLPanel;
import elemental2.promise.Promise;

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
                ClientContext.get().input("输入网址", "网址", "", "", new Callback() {
                    @Override
                    public void onFailure(Object reason) {
                        reject.onInvoke("");
                    }

                    @Override
                    public void onSuccess(Object result) {
                        String url = DataCastor.castToString(result);
                        if (StringUtil.isBlank(url)) {
                            reject.onInvoke("");
                        } else {
                            resolve.onInvoke(url);
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        boolean b = super.initialize(parentModule, parameter);
        String url = DataCastor.castToString(parameter.get());
        frame.setUrl(url);
        return b;
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    interface IFrameWidgetUiBinder extends UiBinder<HTMLPanel, IFrameWidget> {
    }
}