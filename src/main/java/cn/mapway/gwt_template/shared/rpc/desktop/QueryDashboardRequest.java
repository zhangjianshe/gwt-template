package cn.mapway.gwt_template.shared.rpc.desktop;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryDesktopLayoutRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryDesktopLayoutRequest")
public class QueryDashboardRequest implements Serializable, IsSerializable {
    String dashboardName;
}
