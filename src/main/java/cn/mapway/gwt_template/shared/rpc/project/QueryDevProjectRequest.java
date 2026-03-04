package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryDevProjectRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryDevProjectRequest")
public class QueryDevProjectRequest implements Serializable, IsSerializable {

    String projectId;
    String workspaceId;
    String name;

}
