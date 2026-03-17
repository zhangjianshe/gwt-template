package cn.mapway.gwt_template.shared.rpc.project.res;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * DeleteResourceMemberRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("DeleteResourceMemberRequest")
public class DeleteResourceMemberRequest implements Serializable, IsSerializable {
    String resourceId;
    Long userId;
}
