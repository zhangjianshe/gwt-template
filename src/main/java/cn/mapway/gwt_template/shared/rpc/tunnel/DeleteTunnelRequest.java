package cn.mapway.gwt_template.shared.rpc.tunnel;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;
import java.io.Serializable;

@Data
@Doc("DeleteTunnelRequest")
public class DeleteTunnelRequest implements Serializable, IsSerializable {
    String id;
}
