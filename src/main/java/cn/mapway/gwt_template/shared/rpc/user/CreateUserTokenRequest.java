package cn.mapway.gwt_template.shared.rpc.user;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建访问令牌请求
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("创建访问令牌请求")
public class CreateUserTokenRequest implements Serializable, IsSerializable {
    /**
     * 令牌用途说明
     */
    String summary;
}
