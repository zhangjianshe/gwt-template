package cn.mapway.gwt_template.shared.rpc.host;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.CanglingHostEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 主机列表响应（私有主机 + 有权限时的公共主机）。
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("HostListResponse")
public class HostListResponse implements Serializable, IsSerializable {
    List<CanglingHostEntity> hosts;
}
