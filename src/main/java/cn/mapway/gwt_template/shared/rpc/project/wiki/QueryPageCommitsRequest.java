package cn.mapway.gwt_template.shared.rpc.project.wiki;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryPageCommitsRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryPageCommitsRequest")
public class QueryPageCommitsRequest implements Serializable, IsSerializable {
    String pageId;
}
