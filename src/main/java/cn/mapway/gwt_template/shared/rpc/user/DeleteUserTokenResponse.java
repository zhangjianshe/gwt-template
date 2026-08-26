package cn.mapway.gwt_template.shared.rpc.user;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * 删除访问令牌响应
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("删除访问令牌响应")
public class DeleteUserTokenResponse implements Serializable, IsSerializable {
}
