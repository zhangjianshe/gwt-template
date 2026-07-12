package cn.mapway.gwt_template.shared.rpc.desktop;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * DeleteDesktopLayoutRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("DeleteDesktopLayoutRequest")
public class DeleteDashboardRequest implements Serializable, IsSerializable {
    String layoutId;
}
