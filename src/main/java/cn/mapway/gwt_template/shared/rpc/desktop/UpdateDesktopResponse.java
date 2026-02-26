package cn.mapway.gwt_template.shared.rpc.desktop;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DesktopItemEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateDesktopResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateDesktopResponse")
public class UpdateDesktopResponse implements Serializable, IsSerializable {
    DesktopItemEntity item;
}
