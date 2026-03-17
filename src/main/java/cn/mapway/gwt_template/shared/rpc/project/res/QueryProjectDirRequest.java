package cn.mapway.gwt_template.shared.rpc.project.res;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryProjectDirRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryProjectDirRequest")
public class QueryProjectDirRequest implements Serializable, IsSerializable {
    /**
     * 项目资源ID
     */
    String resourceId;
    /**
     * 相对对于资源ID的相对路径
     */
    String path;
}
