package cn.mapway.gwt_template.client.user;

import jsinterop.annotations.JsType;

@JsType(isNative = true)
public class GitNotifyMessage {
    public String phase;
    public String type;
    public String repositoryId;
    public String message;
    public Double progress;
}