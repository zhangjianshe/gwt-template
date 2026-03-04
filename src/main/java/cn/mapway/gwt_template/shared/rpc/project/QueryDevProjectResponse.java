package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryDevProjectResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryDevProjectResponse")
public class QueryDevProjectResponse implements Serializable, IsSerializable {
    List<DevProjectEntity> projects;
}
