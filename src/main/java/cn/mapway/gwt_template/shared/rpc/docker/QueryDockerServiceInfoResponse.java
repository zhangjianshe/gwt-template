package cn.mapway.gwt_template.shared.rpc.docker;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryDockerServiceInfoResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryDockerServiceInfoResponse")
public class QueryDockerServiceInfoResponse implements Serializable, IsSerializable {
    List<DockerServiceInfo> serviceInfoList;
}
