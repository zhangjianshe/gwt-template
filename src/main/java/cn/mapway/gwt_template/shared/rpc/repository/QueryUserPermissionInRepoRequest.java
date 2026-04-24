package cn.mapway.gwt_template.shared.rpc.repository;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryUserPermissionInRepoRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryUserPermissionInRepoRequest")
public class QueryUserPermissionInRepoRequest implements Serializable, IsSerializable {
    String repositoryId;
}
