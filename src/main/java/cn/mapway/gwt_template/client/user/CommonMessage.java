package cn.mapway.gwt_template.client.user;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public class CommonMessage<T> {
    public String topic;
    public T data;

    @JsOverlay
    public final String getTopic() {
        return topic;
    }

    @JsOverlay
    public final boolean isTopic(String topic) {
        return this.topic.equals(topic);
    }

    @JsOverlay
    public final T getData() {
        return data;
    }
}
