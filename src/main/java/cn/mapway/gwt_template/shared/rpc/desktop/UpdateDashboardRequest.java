package cn.mapway.gwt_template.shared.rpc.desktop;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DashboardEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * SaveDesktopLayoutRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("SaveDesktopLayoutRequest")
public class UpdateDashboardRequest implements Serializable, IsSerializable {
    DashboardEntity layout;
}
