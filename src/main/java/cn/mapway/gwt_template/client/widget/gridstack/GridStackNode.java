package cn.mapway.gwt_template.client.widget.gridstack;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class GridStackNode {
    @JsProperty public double x;
    @JsProperty public double y;
    @JsProperty public double w;
    @JsProperty public double h;
    @JsProperty
    public String id;
}
