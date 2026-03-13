package cn.mapway.gwt_template.shared.rpc.workspace;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * ExportDevProjectTaskResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("ExportDevProjectTaskResponse")
public class ExportDevProjectTaskResponse implements Serializable, IsSerializable {
    String body;
    String fileName;
    String mimeType;
}
