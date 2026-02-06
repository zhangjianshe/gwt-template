package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevGroupMemberEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryGroupMemberResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryGroupMemberResponse")
public class QueryGroupMemberResponse implements Serializable, IsSerializable {
    List<DevGroupMemberEntity> members;
}
