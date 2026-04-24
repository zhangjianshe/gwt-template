package cn.mapway.gwt_template.shared.rpc.repository;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * TransferRepositoryRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("TransferRepositoryRequest")
public class TransferRepositoryRequest implements Serializable, IsSerializable {
    String repositoryId;
    Long targetUserId;
}
