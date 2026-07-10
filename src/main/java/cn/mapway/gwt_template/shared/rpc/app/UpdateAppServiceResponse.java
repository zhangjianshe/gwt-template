package cn.mapway.gwt_template.shared.rpc.app;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.AppServiceEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateAppServiceResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateAppServiceResponse")
public class UpdateAppServiceResponse implements Serializable, IsSerializable {
    AppServiceEntity service;
}
