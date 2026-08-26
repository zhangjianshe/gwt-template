package cn.mapway.gwt_template.shared.rpc.user;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * 查询当前用户的访问令牌请求
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("查询当前用户的访问令牌请求")
public class QueryUserTokenRequest implements Serializable, IsSerializable {
}
