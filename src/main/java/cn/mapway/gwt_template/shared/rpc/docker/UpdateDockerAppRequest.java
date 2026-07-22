package cn.mapway.gwt_template.shared.rpc.docker;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DockerAppEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateDockerAppRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateDockerAppRequest")
public class UpdateDockerAppRequest implements Serializable, IsSerializable {
    DockerAppEntity appEntity;
}
