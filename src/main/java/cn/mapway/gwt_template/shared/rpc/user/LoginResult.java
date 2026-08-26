package cn.mapway.gwt_template.shared.rpc.user;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * REST 登录结果：返回用于 API-TOKEN 请求头的持久化 token。
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("LoginResult")
public class LoginResult implements Serializable, IsSerializable {
    String token;
    String userName;
    String nickName;
}
