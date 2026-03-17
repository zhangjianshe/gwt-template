package cn.mapway.gwt_template.shared.rpc.project.res;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.db.DevProjectResourceEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryProjectResourceResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryProjectResourceResponse")
public class QueryProjectResourceResponse implements Serializable, IsSerializable {
    List<DevProjectResourceEntity> resources;
    DevProjectEntity project;
}
