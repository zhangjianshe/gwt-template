package cn.mapway.gwt_template.shared.rpc.project.res;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * DeleteProjectDirFileRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("DeleteProjectDirFileRequest")
public class DeleteProjectDirFileRequest implements Serializable, IsSerializable {
    String resourceId;
    String relativePathName;
}
