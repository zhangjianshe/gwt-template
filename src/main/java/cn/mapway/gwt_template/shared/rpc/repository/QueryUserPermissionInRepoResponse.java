package cn.mapway.gwt_template.shared.rpc.repository;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.VwRepositoryEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryUserPermissionInRepoResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryUserPermissionInRepoResponse")
public class QueryUserPermissionInRepoResponse implements Serializable, IsSerializable {
    VwRepositoryEntity repository;
    String permission;
}
