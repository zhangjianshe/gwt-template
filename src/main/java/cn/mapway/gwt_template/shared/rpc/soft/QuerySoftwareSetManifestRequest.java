package cn.mapway.gwt_template.shared.rpc.soft;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QuerySoftwareSetManifestRequest
 */
@Data
@Doc("QuerySoftwareSetManifestRequest")
public class QuerySoftwareSetManifestRequest implements Serializable, IsSerializable {
    String set;
}
