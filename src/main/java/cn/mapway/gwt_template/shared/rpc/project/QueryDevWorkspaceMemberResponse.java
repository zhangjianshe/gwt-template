package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.rpc.project.module.DevWorkspaceMember;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryDevWorkspaceMemberResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryDevWorkspaceMemberResponse")
public class QueryDevWorkspaceMemberResponse implements Serializable, IsSerializable {
    List<DevWorkspaceMember> members;
}
