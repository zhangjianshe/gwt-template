package cn.mapway.gwt_template.shared.rpc.project.res;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.rpc.project.module.ResItem;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryProjectDirResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryProjectDirResponse")
public class QueryProjectDirResponse implements Serializable, IsSerializable {
    List<ResItem> resources;
    String requestPath;
    String resourceId;
}
