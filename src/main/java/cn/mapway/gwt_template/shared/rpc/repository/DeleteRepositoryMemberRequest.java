package cn.mapway.gwt_template.shared.rpc.repository;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * DeleteProjectMemberRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("DeleteRepositoryMemberRequest")
public class DeleteRepositoryMemberRequest implements Serializable, IsSerializable {
    String repositoryId;
    Long userId;
}
