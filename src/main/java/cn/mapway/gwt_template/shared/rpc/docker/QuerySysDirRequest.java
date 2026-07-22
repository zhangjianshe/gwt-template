package cn.mapway.gwt_template.shared.rpc.docker;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QuerySysDirRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QuerySysDirRequest")
public class QuerySysDirRequest implements Serializable, IsSerializable {
    String path;
}
