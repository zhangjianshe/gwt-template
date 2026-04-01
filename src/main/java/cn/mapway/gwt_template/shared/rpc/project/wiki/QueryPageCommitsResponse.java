package cn.mapway.gwt_template.shared.rpc.project.wiki;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectPageCommitEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryPageCommitsResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryPageCommitsResponse")
public class QueryPageCommitsResponse implements Serializable, IsSerializable {
    List<DevProjectPageCommitEntity> commits;
}
