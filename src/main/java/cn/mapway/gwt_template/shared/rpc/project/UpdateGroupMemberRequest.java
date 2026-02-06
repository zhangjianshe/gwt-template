package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateGroupMemberRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateGroupMemberRequest")
public class UpdateGroupMemberRequest implements Serializable, IsSerializable {
    String groupName;
    Long   userId;
    Integer permission;
}
