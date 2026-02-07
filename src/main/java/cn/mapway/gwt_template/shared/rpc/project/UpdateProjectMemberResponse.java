package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.VwProjectMemberEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateProjectMemberResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateProjectMemberResponse")
public class UpdateProjectMemberResponse implements Serializable, IsSerializable {
    VwProjectMemberEntity member;
}
