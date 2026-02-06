package cn.mapway.gwt_template.shared.rpc.config;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * QueryConfigRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryConfigRequest")
public class QueryConfigRequest implements Serializable, IsSerializable {
    List<String> configKeys = new ArrayList<String>();
}
