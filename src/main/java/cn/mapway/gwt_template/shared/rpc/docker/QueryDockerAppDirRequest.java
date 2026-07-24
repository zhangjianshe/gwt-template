package cn.mapway.gwt_template.shared.rpc.docker;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryDockerAppDirRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryDockerAppDirRequest")
public class QueryDockerAppDirRequest implements Serializable, IsSerializable {
    String dockerAppId;
    String path;
}
