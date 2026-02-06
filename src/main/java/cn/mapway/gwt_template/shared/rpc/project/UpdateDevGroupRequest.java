package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevGroupEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateDevGroupRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateDevGroupRequest")
public class UpdateDevGroupRequest implements Serializable, IsSerializable {
    DevGroupEntity devGroup;
}
