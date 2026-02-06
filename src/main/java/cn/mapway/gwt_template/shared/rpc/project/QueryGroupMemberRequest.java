package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryGroupMemberRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryGroupMemberRequest")
public class QueryGroupMemberRequest implements Serializable, IsSerializable {
    String groupName;
}
