package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.VwProjectMemberEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryProjectMemberResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryProjectMemberResponse")
public class QueryProjectMemberResponse implements Serializable, IsSerializable {
    List<VwProjectMemberEntity> members;
    Integer currentUserPermission;
}
