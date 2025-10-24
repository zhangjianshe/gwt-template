package cn.mapway.gwt_template.shared.rpc.config;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryConfigListRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryConfigListRequest")
public class QueryConfigListRequest implements Serializable, IsSerializable {
    List<String> keys;
}
