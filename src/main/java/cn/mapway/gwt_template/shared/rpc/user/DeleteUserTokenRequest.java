package cn.mapway.gwt_template.shared.rpc.user;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * 删除访问令牌请求
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("删除访问令牌请求")
public class DeleteUserTokenRequest implements Serializable, IsSerializable {
    /**
     * 令牌 ID（即 token 本身）
     */
    String tokenId;
}
