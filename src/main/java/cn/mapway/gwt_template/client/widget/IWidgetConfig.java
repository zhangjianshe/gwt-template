package cn.mapway.gwt_template.client.widget;

import com.google.gwt.user.client.ui.Widget;
import elemental2.promise.Promise;

/**
 * 提供配置窗口
 */
public interface IWidgetConfig {
    Promise<Object> showConfig(Object parameter);

    default Widget getWidgetTools() {
        return null;
    }

}
