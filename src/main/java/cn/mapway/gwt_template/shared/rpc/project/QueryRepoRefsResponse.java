package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.rpc.project.git.GitRef;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryRepoRefsResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryRepoRefsResponse")
public class QueryRepoRefsResponse implements Serializable, IsSerializable {
    List<GitRef> refs;
    String defaultBranch;
}
