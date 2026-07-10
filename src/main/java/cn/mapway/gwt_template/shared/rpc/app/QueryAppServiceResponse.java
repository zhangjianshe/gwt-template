package cn.mapway.gwt_template.shared.rpc.app;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.AppServiceEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryAppServiceResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryAppServiceResponse")
public class QueryAppServiceResponse implements Serializable, IsSerializable {
    List<AppServiceEntity> services;
}
