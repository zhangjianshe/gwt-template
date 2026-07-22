package cn.mapway.gwt_template.shared.rpc.docker;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryDockerAppsRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryDockerAppsRequest")
public class QueryDockerAppsRequest implements Serializable, IsSerializable {
}
