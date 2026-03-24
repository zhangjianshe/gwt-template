package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.rpc.project.module.PreviewData;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * ViewAttachmentFileResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("ViewAttachmentFileResponse")
public class ViewAttachmentFileResponse implements Serializable, IsSerializable {
    PreviewData previewData;
}
