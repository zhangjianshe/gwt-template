package cn.mapway.gwt_template.client.rpc;


import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.ui.client.tools.DataBus;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * AsyncAdaptor
 * HTTP RPC回调代理 简化操作
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
public abstract class AsyncAdaptor<T extends RpcResult> implements AsyncCallback<T> {

    private String topic;

    public AsyncAdaptor(String topic) {
        this.topic = topic;
    }

    public AsyncAdaptor() {
    }


    @Override
    public void onFailure(Throwable caught) {
        sendMsg();
        ClientContext.get().hideWaiting();
    }

    /**
     * result 一定是成功的对象
     *
     * @param result
     */
    public abstract void onData(T result);

    /**
     * result 一定是成功的对象
     *
     * @param result
     */
    public  boolean onError(T result){

        return false;
    }

    @Override
    public void onSuccess(T result) {
        if (result.isSuccess()) {
            sendMsg();
            ClientContext.get().hideWaiting();
            onData(result);
        } else {
            if(!onError(result)){
                onFailure(new Exception(result.getMessage()));
                ClientContext.processServiceCode(result);
                ClientContext.get().toast(0, 0, result.getMessage());
            }
        }
    }

    private void sendMsg() {
        if (topic != null) {
            DataBus.get().fire(topic, 0, 0);
        }
    }
}
