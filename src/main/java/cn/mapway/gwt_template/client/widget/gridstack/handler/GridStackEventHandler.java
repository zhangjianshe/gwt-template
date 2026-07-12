package cn.mapway.gwt_template.client.widget.gridstack.handler;

import jsinterop.annotations.JsFunction;

/**
 * 对应 GridStackEventHandler
 * 用于 'enable', 'disable' 等无需附加参数的纯状态事件
 */
@JsFunction
@FunctionalInterface
public interface GridStackEventHandler {
    void onEvent(elemental2.dom.Event event);
}