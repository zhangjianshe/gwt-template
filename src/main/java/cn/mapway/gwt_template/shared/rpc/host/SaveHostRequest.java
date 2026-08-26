package cn.mapway.gwt_template.shared.rpc.host;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.CanglingHostEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * 保存主机请求。
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("SaveHostRequest")
public class SaveHostRequest implements Serializable, IsSerializable {
    CanglingHostEntity host;
}
