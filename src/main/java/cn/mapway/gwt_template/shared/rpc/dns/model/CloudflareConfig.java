package cn.mapway.gwt_template.shared.rpc.dns.model;

import jsinterop.annotations.JsType;


@JsType(isNative = true)
public class CloudflareConfig {
    public String name;
    public String token;
    public String zoneId;
    public String suffix;
}
