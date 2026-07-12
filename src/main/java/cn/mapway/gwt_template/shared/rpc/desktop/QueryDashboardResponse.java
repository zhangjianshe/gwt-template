package cn.mapway.gwt_template.shared.rpc.desktop;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DashboardEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryDesktopLayoutResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryDesktopLayoutResponse")
public class QueryDashboardResponse implements Serializable, IsSerializable {
    List<DashboardEntity> dashboards;
}
