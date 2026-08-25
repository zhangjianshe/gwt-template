package cn.mapway.gwt_template.shared.rpc.docker;

import cn.mapway.document.annotation.ApiField;
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
    String dockerAppId;
    Integer action;
    @ApiField("服务名称 如果为空 重启所有的应用  否则只重启某个应用")
    String serviceName = "";
}
