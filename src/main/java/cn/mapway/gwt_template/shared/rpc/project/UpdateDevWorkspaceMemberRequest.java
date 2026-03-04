package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevWorkspaceMemberEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateDevWorkspaceMemberRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateDevWorkspaceMemberRequest")
public class UpdateDevWorkspaceMemberRequest implements Serializable, IsSerializable {
    DevWorkspaceMemberEntity member;
}
