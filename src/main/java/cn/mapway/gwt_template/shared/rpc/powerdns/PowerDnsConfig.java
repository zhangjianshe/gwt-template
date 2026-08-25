package cn.mapway.gwt_template.shared.rpc.powerdns;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * Power dns configuration
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class PowerDnsConfig {
    public String basePath;
    public String token;
}
