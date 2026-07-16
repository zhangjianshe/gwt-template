package cn.mapway.gwt_template.shared.rpc.desktop;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class DashboardItemData {
    public String id;
    public String moduleCode;
    public Double x;
    public Double y;
    public Double w;
    public Double h;
    public String parameter;
    public Boolean showHeader;
}
