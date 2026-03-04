package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * DeleteDevWorkspaceFolderRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("DeleteDevWorkspaceFolderRequest")
public class DeleteDevWorkspaceFolderRequest implements Serializable, IsSerializable {
    String folderId;
}
