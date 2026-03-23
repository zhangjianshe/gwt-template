package cn.mapway.gwt_template.shared.rpc.desktop;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DesktopItemEntity;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.db.DevWorkspaceEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryDesktopResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryDesktopResponse")
public class QueryDesktopResponse implements Serializable, IsSerializable {
    List<DesktopItemEntity> items;
    List<DevWorkspaceEntity> workspaces;
    List<DevProjectEntity> favoriteProjects;
}
