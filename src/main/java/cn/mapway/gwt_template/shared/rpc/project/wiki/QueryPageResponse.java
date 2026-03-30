package cn.mapway.gwt_template.shared.rpc.project.wiki;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectPageEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryPageResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryPageResponse")
public class QueryPageResponse implements Serializable, IsSerializable {
    List<DevProjectPageEntity> rootPages;
}
