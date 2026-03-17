package cn.mapway.gwt_template.shared.rpc.project.res;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectResourceEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateProjectResourceRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateProjectResourceRequest")
public class UpdateProjectResourceRequest implements Serializable, IsSerializable {
    DevProjectResourceEntity resource;
}
