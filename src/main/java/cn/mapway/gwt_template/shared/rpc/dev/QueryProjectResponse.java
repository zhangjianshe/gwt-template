package cn.mapway.gwt_template.shared.rpc.dev;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryProjectResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryProjectResponse")
public class QueryProjectResponse implements Serializable, IsSerializable {
    List<DevProjectEntity> projects;
}
