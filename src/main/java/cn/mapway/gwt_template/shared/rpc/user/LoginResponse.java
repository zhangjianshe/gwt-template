package cn.mapway.gwt_template.shared.rpc.user;

import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * LoginResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
public class LoginResponse implements Serializable, IsSerializable {
    LoginUser user;
}
