package cn.mapway.gwt_template.shared.rpc.soft;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 软件集 manifest：包含该集下全部软件及其文件。
 */
@Data
@Doc("QuerySoftwareSetManifestResponse")
public class QuerySoftwareSetManifestResponse implements Serializable, IsSerializable {
    String name;
    List<SoftwareManifest> softwares;
}
