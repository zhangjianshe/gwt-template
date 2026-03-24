package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * ViewAttachmentFileRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("ViewAttachmentFileRequest")
public class ViewAttachmentFileRequest implements Serializable, IsSerializable {
    String taskId;
    String relPathName;
}
