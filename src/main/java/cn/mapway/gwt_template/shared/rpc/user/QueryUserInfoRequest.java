package cn.mapway.gwt_template.shared.rpc.user;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryUserInfoRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryUserInfoRequest")
public class QueryUserInfoRequest implements Serializable, IsSerializable {
    List<Long> userIdList;
}
