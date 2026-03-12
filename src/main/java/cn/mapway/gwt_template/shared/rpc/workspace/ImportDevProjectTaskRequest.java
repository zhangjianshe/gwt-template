package cn.mapway.gwt_template.shared.rpc.workspace;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * ImportDevProjectTaskRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("ImportDevProjectTaskRequest")
public class ImportDevProjectTaskRequest implements Serializable, IsSerializable {
    String projectId;
    String parentTaskId;
    String body;
}
