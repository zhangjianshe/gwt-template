package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevGroupEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryDevGroupResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryDevGroupResponse")
public class QueryDevGroupResponse implements Serializable, IsSerializable {
    List<DevGroupEntity> groups;
}
