package cn.mapway.gwt_template.shared.rpc.dev;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryProjectBuildRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryProjectBuildRequest")
public class QueryProjectBuildRequest implements Serializable, IsSerializable {
    String projectId;
    Integer page;
    Integer pageSize;
}
