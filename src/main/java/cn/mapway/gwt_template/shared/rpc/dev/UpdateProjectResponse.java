package cn.mapway.gwt_template.shared.rpc.dev;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateProjectResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateProjectResponse")
public class UpdateProjectResponse implements Serializable, IsSerializable {
    DevProjectEntity project;
}
