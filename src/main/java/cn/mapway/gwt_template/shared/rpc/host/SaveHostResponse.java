package cn.mapway.gwt_template.shared.rpc.host;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.CanglingHostEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * 保存主机响应。
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("SaveHostResponse")
public class SaveHostResponse implements Serializable, IsSerializable {
    CanglingHostEntity host;
}
