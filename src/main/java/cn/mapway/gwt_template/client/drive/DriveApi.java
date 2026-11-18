package cn.mapway.gwt_template.client.drive;

import cn.mapway.gwt_template.client.ClientContext;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.*;
import elemental2.dom.File;
import elemental2.dom.XMLHttpRequest;

/** Same-origin JSON and raw upload client. Credentials never enter URLs. */
final class DriveApi {
  interface Callback {
    void success(JSONObject data);

    void failure(String message);
  }

  interface Progress {
    void update(int percent);
  }

  static String base() {
    return GWT.getHostPageBaseURL() + "api/v1/drive";
  }

  static String string(JSONObject value, String key) {
    JSONValue field = value == null ? null : value.get(key);
    return field != null && field.isString() != null ? field.isString().stringValue() : "";
  }

  static double number(JSONObject value, String key) {
    JSONValue field = value == null ? null : value.get(key);
    return field != null && field.isNumber() != null ? field.isNumber().doubleValue() : 0;
  }

  static boolean directory(JSONObject entry) {
    JSONValue field = entry.get("directory");
    return field != null && field.isBoolean() != null && field.isBoolean().booleanValue();
  }

  static JSONArray array(JSONObject data, String key) {
    JSONValue field = data.get(key);
    return field != null && field.isArray() != null ? field.isArray() : new JSONArray();
  }

  static JSONObject object(String key, String value) {
    JSONObject result = new JSONObject();
    result.put(key, new JSONString(value));
    return result;
  }

  static void request(String method, String path, JSONObject body, Callback callback) {
    XMLHttpRequest xhr = prepare(method, path, callback);
    xhr.timeout = 30000;
    if (body != null) {
      xhr.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
      xhr.send(body.toString());
    } else xhr.send();
  }

  static XMLHttpRequest upload(String path, File file, Progress progress, Callback callback) {
    XMLHttpRequest xhr = prepare("PUT", path, callback);
    xhr.setRequestHeader("Content-Type", "application/octet-stream");
    xhr.upload.onprogress =
        event -> {
          if (event.lengthComputable)
            progress.update((int) Math.round(event.loaded * 100 / event.total));
        };
    xhr.send(file);
    return xhr;
  }

  private static XMLHttpRequest prepare(String method, String path, Callback callback) {
    XMLHttpRequest xhr = new XMLHttpRequest();
    xhr.open(method, base() + path);
    String token = ClientContext.adminCookie();
    if (token != null && !token.isEmpty()) xhr.setRequestHeader("API-TOKEN", token);
    xhr.onload =
        event -> {
          try {
            JSONObject result = JSONParser.parseStrict(xhr.responseText).isObject();
            if (result != null && xhr.status == 200 && number(result, "code") == 200) {
              JSONValue data = result.get("data");
              callback.success(
                  data == null || data.isObject() == null ? new JSONObject() : data.isObject());
            } else {
              String message = string(result, "message");
              callback.failure(message.isEmpty() ? "请求失败（HTTP " + xhr.status + "）" : message);
            }
          } catch (RuntimeException exception) {
            callback.failure("服务器响应异常，请刷新后重试");
          }
        };
    xhr.onerror =
        event -> {
          callback.failure("网络连接失败，请检查网络后重试");
          return true;
        };
    xhr.ontimeout = event -> callback.failure("请求超时，请重试");
    xhr.onabort = event -> callback.failure("上传已取消");
    return xhr;
  }
}
