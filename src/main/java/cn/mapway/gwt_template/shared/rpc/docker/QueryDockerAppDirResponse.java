package cn.mapway.gwt_template.shared.rpc.docker;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.rpc.project.module.ResItem;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryDockerAppDirResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryDockerAppDirResponse")
public class QueryDockerAppDirResponse implements Serializable, IsSerializable {
    List<ResItem> files;
    String path;
}
