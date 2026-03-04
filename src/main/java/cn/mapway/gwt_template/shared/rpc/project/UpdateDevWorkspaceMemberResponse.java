package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevWorkspaceMemberEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateDevWorkspaceMemberResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateDevWorkspaceMemberResponse")
public class UpdateDevWorkspaceMemberResponse implements Serializable, IsSerializable {
    DevWorkspaceMemberEntity member;
}
