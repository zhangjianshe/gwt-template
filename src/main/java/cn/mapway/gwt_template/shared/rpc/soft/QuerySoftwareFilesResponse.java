package cn.mapway.gwt_template.shared.rpc.soft;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.SysSoftwareFileEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QuerySoftwareFilesResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QuerySoftwareFilesResponse")
public class QuerySoftwareFilesResponse implements Serializable, IsSerializable {
    List<SysSoftwareFileEntity> files;
}
