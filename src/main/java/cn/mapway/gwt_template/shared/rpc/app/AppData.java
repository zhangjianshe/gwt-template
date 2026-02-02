package cn.mapway.gwt_template.shared.rpc.app;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
@Data
public class AppData implements Serializable, IsSerializable {
    String logo;
}
