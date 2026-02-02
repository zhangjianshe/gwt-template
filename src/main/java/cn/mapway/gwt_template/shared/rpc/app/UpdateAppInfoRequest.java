package cn.mapway.gwt_template.shared.rpc.app;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateAppInfoRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateAppInfoRequest")
public class UpdateAppInfoRequest implements Serializable, IsSerializable {
    AppData appData;
}
