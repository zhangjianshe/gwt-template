package cn.mapway.gwt_template.shared.rpc.workspace;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * ExportDevProjectTaskRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("ExportDevProjectTaskRequest")
public class ExportDevProjectTaskRequest implements Serializable, IsSerializable {
    String projectId;
    String type;
}
