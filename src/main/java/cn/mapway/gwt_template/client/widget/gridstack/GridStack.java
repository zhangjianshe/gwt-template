package cn.mapway.gwt_template.client.widget.gridstack;

import cn.mapway.gwt_template.client.widget.gridstack.handler.GridStackElementHandler;
import cn.mapway.gwt_template.client.widget.gridstack.handler.GridStackEventHandler;
import cn.mapway.gwt_template.client.widget.gridstack.handler.GridStackNodesHandler;
import elemental2.dom.Element;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "GridStack")
public class GridStack {

    @JsMethod
    public static native GridStack init(GridStackOptions options, Element element);

    @JsMethod
    public native Element makeWidget(Element el);

    @JsMethod
    public native void removeWidget(Element el, boolean removeDOM);

    @JsMethod
    public native GridStackNode[] save(boolean saveContent);

    /**
     * 重载 1：针对节点批变动事件 ('change' | 'added' | 'removed')
     * 第二个参数在 JS 中将被直接作为 GridStackNode[] 处理
     */
    @JsMethod(name = "on")
    public native GridStack on(String eventName, GridStackNodesHandler handler);

    /**
     * 重载 2：针对单组件拖拽/缩放精准控制事件 ('dragstop' | 'resizestop' | 'resize')
     * 第二个参数在 JS 中是一个原生的 HTMLElement 节点
     */
    @JsMethod(name = "on")
    public native GridStack on(String eventName, GridStackElementHandler handler);

    /**
     * 重载 3：针对状态控制事件 ('enable' | 'disable')
     */
    @JsMethod(name = "on")
    public native GridStack on(String eventName, GridStackEventHandler handler);

    @JsMethod
    public native GridStack off(String eventName);

    @JsOverlay
    final public void onChange(GridStackNodesHandler handler) {
        on("change", handler);
    }

    @JsOverlay
    final public void onResize(GridStackElementHandler handler) {
        on("resize", handler);
    }

    @JsMethod
    public native GridStack offAll();

}
