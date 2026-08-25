package cn.mapway.gwt_template.shared.rpc.desktop;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryDesktopRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryDesktopRequest")
public class QueryDesktopRequest implements Serializable, IsSerializable {
    boolean fetchMainBoard;
    boolean fetchShortcut;
    boolean fetchProjects;
    boolean fetchWorkspaces;
}
