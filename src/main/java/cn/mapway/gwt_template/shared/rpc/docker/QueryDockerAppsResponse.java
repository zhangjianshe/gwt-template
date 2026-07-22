package cn.mapway.gwt_template.shared.rpc.docker;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DockerAppEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryDockerAppsResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryDockerAppsResponse")
public class QueryDockerAppsResponse implements Serializable, IsSerializable {
    List<DockerAppEntity> apps;
}
