package cn.mapway.gwt_template.shared.rpc.user;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * RegisterUserRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("RegisterUserRequest")
public class RegisterUserRequest implements Serializable, IsSerializable {
    String user;
    String email;
    String pwd;
}
