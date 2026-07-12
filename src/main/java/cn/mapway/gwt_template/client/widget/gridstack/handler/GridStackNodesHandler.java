package cn.mapway.gwt_template.client.widget.gridstack.handler;

import cn.mapway.gwt_template.client.widget.gridstack.GridStackNode;
import jsinterop.annotations.JsFunction;

/**
 * 对应 GridStackNodesHandler
 * 用于 'change', 'added', 'removed' 事件
 */
@JsFunction
@FunctionalInterface
public interface GridStackNodesHandler {
    void onEvent(elemental2.dom.Event event, GridStackNode[] nodes);
}