package cn.mapway.gwt_template.shared.rpc.dev;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.VwRepositoryEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryRepositoryResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryRepositoryResponse")
public class QueryRepositoryResponse implements Serializable, IsSerializable {
    List<VwRepositoryEntity> repositories;
}
