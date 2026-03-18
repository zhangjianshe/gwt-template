package cn.mapway.gwt_template.shared.rpc.project.res;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.rpc.project.module.ResourceMember;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryResourceMemberResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryResourceMemberResponse")
public class QueryResourceMemberResponse implements Serializable, IsSerializable {
    List<ResourceMember> members;
    String currentPermission;
}
