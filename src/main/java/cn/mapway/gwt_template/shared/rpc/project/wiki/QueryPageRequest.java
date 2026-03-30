package cn.mapway.gwt_template.shared.rpc.project.wiki;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryPageRequest
 * 查询一个项目的页面列表
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryPageRequest")
public class QueryPageRequest implements Serializable, IsSerializable {
    String projectId;
}
