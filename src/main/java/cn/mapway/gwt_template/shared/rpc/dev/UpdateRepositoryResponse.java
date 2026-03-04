package cn.mapway.gwt_template.shared.rpc.dev;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.VwRepositoryEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateRepositoryResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateRepositoryResponse")
public class UpdateRepositoryResponse implements Serializable, IsSerializable {
    VwRepositoryEntity repository;
}
