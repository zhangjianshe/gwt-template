package cn.mapway.gwt_template.client.widget.file;

import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.Logs;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.HasCommonHandlers;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import elemental2.dom.Blob;
import elemental2.dom.File;
import elemental2.dom.FormData;
import elemental2.dom.XMLHttpRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * XmlHttpUploader
 *
 * @author zhang
 */
public class XmlHttpUploader implements HasCommonHandlers, IData {
    Object data;
    XMLHttpRequest request;
    SimpleEventBus simpleEventBus;
    boolean transfering = false;
    Map<String, String> dataList = new HashMap<>();
    Map<String, String> heads = new HashMap<>();
    String action;
    String fileFieldName = "file";

    public XmlHttpUploader() {
        simpleEventBus = new SimpleEventBus();
        request = new XMLHttpRequest();
        request.upload.onprogress = (progressEvent) -> {
            fireEvent(CommonEvent.create(CommonEvent.PROGRESS, progressEvent));
        };
        request.onabort = (abortEvent) -> {
            transfering = false;
            fireEvent(CommonEvent.create(CommonEvent.ABORT, abortEvent));
        };
        request.onerror = (e) -> {
            transfering = false;
            fireEvent(CommonEvent.create(CommonEvent.ERROR, e));
            return true;
        };
        request.onload = (o) -> {
            transfering = true;
            fireEvent(CommonEvent.create(CommonEvent.LOAD, o));
        };
        request.onloadstart = (o) -> {
            transfering = true;
            fireEvent(CommonEvent.create(CommonEvent.LOADSTART, o));
        };
        request.onloadend = (o) -> {
            transfering = false;
            CommonEvent event = CommonEvent.create(CommonEvent.LOADEND, o);
            fireEvent(event);
        };
        request.onreadystatechange = (readyStateEvent) -> {
            fireEvent(CommonEvent.create(CommonEvent.READYSTATECHANGE, readyStateEvent));

            return "";
        };
    }

    public void setHeads(String key, String value) {
        heads.put(key, value);
    }

    public void clearHeads() {
        heads.clear();
    }

    public void setData(String key, String value) {
        dataList.put(key, value);
    }

    public void clearData() {
        dataList.clear();
    }

    /**
     * 上传文件的接受地址
     *
     * @param action
     */
    public void setAction(String action) {
        this.action = action;
    }

    public void setFileFieldName(String fileFieldName) {
        this.fileFieldName = fileFieldName;
    }

    public void start(File file) {
        FormData formData = new FormData();
        for (String key : dataList.keySet()) {
            Logs.info("key:" + key + " value:" + dataList.get(key));
            formData.append(key, dataList.get(key));
        }
        formData.append(fileFieldName, file);
        request.open("POST", action, true);
        Logs.info("------------------------------------");
        for (String key : heads.keySet()) {
            Logs.info("key:" + key + " value:" + heads.get(key));
            request.setRequestHeader(key, heads.get(key));
        }
        request.send(formData);
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public void setData(Object obj) {
        data = obj;
    }

    @Override
    public HandlerRegistration addCommonHandler(CommonEventHandler handler) {
        return simpleEventBus.addHandler(CommonEvent.TYPE, handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        simpleEventBus.fireEvent(event);
    }

    public boolean isTransfering() {
        return transfering;
    }

    public void abort() {
        request.abort();
    }

    public void start(Blob slice, String s) {
        FormData formData = new FormData();
        for (String key : dataList.keySet()) {
            formData.append(key, dataList.get(key));
        }
        formData.append(fileFieldName, slice, s);
        request.open("POST", action, true);
        for (String key : heads.keySet()) {
            request.setRequestHeader(key, heads.get(key));
        }
        request.send(formData);
    }
}
