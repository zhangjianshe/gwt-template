package cn.mapway.gwt_template.shared.rpc.soft;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 软件集 manifest 中的单个软件。
 */
@Data
@Doc("SoftwareManifest")
public class SoftwareManifest implements Serializable, IsSerializable {
    String id;
    String name;
    String code;
    String softwareSet;
    String summary;
    String logo;
    List<SoftwareFileManifest> files;
}
