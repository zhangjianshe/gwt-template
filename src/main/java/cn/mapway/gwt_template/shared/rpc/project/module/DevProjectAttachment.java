package cn.mapway.gwt_template.shared.rpc.project.module;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * 项目的相关附件
 */
@Doc("项目附件")
@Data
public class DevProjectAttachment implements Serializable, IsSerializable {
    String projectId;
    String name;
    String mimeType;
    Long size;
    String fileName;
}
