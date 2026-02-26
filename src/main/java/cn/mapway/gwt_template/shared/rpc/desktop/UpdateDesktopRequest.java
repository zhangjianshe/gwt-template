package cn.mapway.gwt_template.shared.rpc.desktop;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DesktopItemEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateDesktopRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateDesktopRequest")
public class UpdateDesktopRequest implements Serializable, IsSerializable {
    DesktopItemEntity item;
}
