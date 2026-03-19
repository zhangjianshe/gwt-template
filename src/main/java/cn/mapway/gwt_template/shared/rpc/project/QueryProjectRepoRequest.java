package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryProjectRepoRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryProjectRepoRequest")
public class QueryProjectRepoRequest implements Serializable, IsSerializable {
    String projectId;
}
