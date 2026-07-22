package cn.mapway.gwt_template.shared.rpc.docker;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.rpc.project.module.ResItem;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QuerySysDirResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QuerySysDirResponse")
public class QuerySysDirResponse implements Serializable, IsSerializable {
    List<ResItem> dirs;
    String path;
}
