package cn.mapway.gwt_template.shared.rpc.project.res;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * AddResourceMemberRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("AddResourceMemberRequest")
public class AddResourceMemberRequest implements Serializable, IsSerializable {
    String resourceId;
    Long userId;
    String permission;
}
