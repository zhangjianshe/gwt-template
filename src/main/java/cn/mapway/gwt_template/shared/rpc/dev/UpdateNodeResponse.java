package cn.mapway.gwt_template.shared.rpc.dev;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevNodeEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateNodeResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateNodeResponse")
public class UpdateNodeResponse implements Serializable, IsSerializable {
    DevNodeEntity node;
}
