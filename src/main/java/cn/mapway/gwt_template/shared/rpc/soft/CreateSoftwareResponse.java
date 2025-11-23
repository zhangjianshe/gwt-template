package cn.mapway.gwt_template.shared.rpc.soft;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.SysSoftwareEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * CreateSoftwareResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("CreateSoftwareResponse")
public class CreateSoftwareResponse implements Serializable, IsSerializable {
    SysSoftwareEntity software;
}
