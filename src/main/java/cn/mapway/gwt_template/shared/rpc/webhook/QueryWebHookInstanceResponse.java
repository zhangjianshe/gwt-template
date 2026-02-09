package cn.mapway.gwt_template.shared.rpc.webhook;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.WebHookInstanceEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryWebHookInstanceResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryWebHookInstanceResponse")
public class QueryWebHookInstanceResponse implements Serializable, IsSerializable {
    List<WebHookInstanceEntity> instances;
    Integer total;
    Integer page;
    Integer pageSize;
}
