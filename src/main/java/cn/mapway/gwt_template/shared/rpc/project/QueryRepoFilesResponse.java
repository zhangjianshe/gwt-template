package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryRepoFilesResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryRepoFilesResponse")
public class QueryRepoFilesResponse implements Serializable, IsSerializable {
    List<RepoItem> items;
}
