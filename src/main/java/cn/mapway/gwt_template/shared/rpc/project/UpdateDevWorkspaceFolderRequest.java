package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevWorkspaceFolderEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateDevWorkspaceFolderRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateDevWorkspaceFolderRequest")
public class UpdateDevWorkspaceFolderRequest implements Serializable, IsSerializable {
    DevWorkspaceFolderEntity folder;
}
