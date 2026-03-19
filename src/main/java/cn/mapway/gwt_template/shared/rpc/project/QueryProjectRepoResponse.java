package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.VwRepositoryEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryProjectRepoResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryProjectRepoResponse")
public class QueryProjectRepoResponse implements Serializable, IsSerializable {
    List<VwRepositoryEntity> repositories;
    String currentUserPermission;
}
