package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateFavoriteProjectRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateFavoriteProjectRequest")
public class UpdateFavoriteProjectRequest implements Serializable, IsSerializable {
    String projectId;
    Boolean favorite;
}
