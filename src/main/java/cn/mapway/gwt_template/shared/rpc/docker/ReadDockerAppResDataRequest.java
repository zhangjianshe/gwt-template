package cn.mapway.gwt_template.shared.rpc.docker;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * ReadDockerAppResDataRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("ReadDockerAppResDataRequest")
public class ReadDockerAppResDataRequest implements Serializable, IsSerializable {
    String appId;
    String filePathName;
}
