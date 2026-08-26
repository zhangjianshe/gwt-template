package cn.mapway.gwt_template.shared.rpc.host;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * 主机列表请求（无参数）。
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("HostListRequest")
public class HostListRequest implements Serializable, IsSerializable {
}
