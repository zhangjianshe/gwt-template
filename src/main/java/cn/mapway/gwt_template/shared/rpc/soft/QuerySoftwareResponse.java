package cn.mapway.gwt_template.shared.rpc.soft;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.SysSoftwareEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QuerySoftwareResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QuerySoftwareResponse")
public class QuerySoftwareResponse implements Serializable, IsSerializable {
    List<SysSoftwareEntity> softwares;
}
