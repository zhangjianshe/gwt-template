package cn.mapway.gwt_template.shared.rpc.soft;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * 软件集 manifest 中的单个文件。
 */
@Data
@Doc("SoftwareFileManifest")
public class SoftwareFileManifest implements Serializable, IsSerializable {
    String id;
    String name;
    String version;
    String os;
    String arch;
    Long size;
    String hash;
    String url;
    String summary;
}
