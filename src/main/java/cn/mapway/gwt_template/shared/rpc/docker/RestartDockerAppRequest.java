package cn.mapway.gwt_template.shared.rpc.docker;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * RestartDockerAppRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("RestartDockerAppRequest")
public class RestartDockerAppRequest implements Serializable, IsSerializable {
}
