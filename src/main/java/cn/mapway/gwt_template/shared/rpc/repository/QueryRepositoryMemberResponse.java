package cn.mapway.gwt_template.shared.rpc.repository;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.VwRepositoryMemberEntity;
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
@Doc("QueryRepositoryMemberResponse")
public class QueryRepositoryMemberResponse implements Serializable, IsSerializable {
    List<VwRepositoryMemberEntity> members;
    Integer currentUserPermission;
}
