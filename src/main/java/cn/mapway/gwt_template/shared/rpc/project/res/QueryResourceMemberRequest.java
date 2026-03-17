package cn.mapway.gwt_template.shared.rpc.project.res;

import cn.mapway.document.annotation.ApiField;
import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryResourceMemberRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryResourceMemberRequest")
public class QueryResourceMemberRequest implements Serializable, IsSerializable {
    @ApiField("项目资源ID")
    String resourceId;
}
