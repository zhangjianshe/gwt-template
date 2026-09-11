package cn.mapway.gwt_template.shared.rpc.tunnel;

import cn.mapway.gwt_template.shared.db.CanglingTunnelEntity;
import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
@Doc("TunnelListResponse")
public class TunnelListResponse implements Serializable, IsSerializable {
    List<CanglingTunnelEntity> tunnels;
}
