package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * DeleteTaskAttachmentsRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("DeleteTaskAttachmentsRequest")
public class DeleteTaskAttachmentsRequest implements Serializable, IsSerializable {
    String taskId;
    String pathName;
}
