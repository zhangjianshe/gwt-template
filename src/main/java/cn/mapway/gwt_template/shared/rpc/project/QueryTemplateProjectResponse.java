package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryTemplateProjectResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryTemplateProjectResponse")
public class QueryTemplateProjectResponse implements Serializable, IsSerializable {
    List<DevProjectEntity> projects;
}
