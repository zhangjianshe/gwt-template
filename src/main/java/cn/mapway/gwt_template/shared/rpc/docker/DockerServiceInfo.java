package cn.mapway.gwt_template.shared.rpc.docker;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

@Data
public class DockerServiceInfo  implements Serializable, IsSerializable {
    String serviceName;
    String image;
    String status;
}
