package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevGroupMemberEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateGroupMemberResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateGroupMemberResponse")
public class UpdateGroupMemberResponse implements Serializable, IsSerializable {
    DevGroupMemberEntity member;
}
