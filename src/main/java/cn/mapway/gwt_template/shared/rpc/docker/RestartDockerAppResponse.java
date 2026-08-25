package cn.mapway.gwt_template.shared.rpc.docker;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * RestartDockerAppResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("RestartDockerAppResponse")
public class RestartDockerAppResponse implements Serializable, IsSerializable {
}
