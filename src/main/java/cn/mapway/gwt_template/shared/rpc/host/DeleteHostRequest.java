package cn.mapway.gwt_template.shared.rpc.host;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * 删除主机请求。
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("DeleteHostRequest")
public class DeleteHostRequest implements Serializable, IsSerializable {
    String id;
}
