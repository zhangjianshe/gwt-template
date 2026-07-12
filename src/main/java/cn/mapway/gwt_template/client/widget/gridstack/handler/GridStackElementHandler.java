package cn.mapway.gwt_template.client.widget.gridstack.handler;

import jsinterop.annotations.JsFunction;
import elemental2.dom.HTMLElement;

/**
 * 对应 GridStackElementHandler
 * 用于 'dragstop', 'resizestop', 'drag', 'resize' 等事件
 */
@JsFunction
@FunctionalInterface
public interface GridStackElementHandler {
    void onEvent(elemental2.dom.Event event, HTMLElement element);
}