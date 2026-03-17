package cn.mapway.gwt_template.shared.rpc.project.res;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * CreateProjectDirFileRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("CreateProjectDirFileRequest")
public class CreateProjectDirFileRequest implements Serializable, IsSerializable {
    String resourceId;
    String parentPath;
    String name;
    Boolean isDir;
}
