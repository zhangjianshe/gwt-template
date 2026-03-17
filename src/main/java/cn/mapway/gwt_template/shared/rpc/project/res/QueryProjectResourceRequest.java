package cn.mapway.gwt_template.shared.rpc.project.res;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryProjectResourceRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryProjectResourceRequest")
public class QueryProjectResourceRequest implements Serializable, IsSerializable {
    String projectId;
}
