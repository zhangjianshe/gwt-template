package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.rpc.project.module.ResItem;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryTaskAttachmentsResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryTaskAttachmentsResponse")
public class QueryTaskAttachmentsResponse implements Serializable, IsSerializable {
    List<ResItem> resources;
}
