package cn.mapway.gwt_template.shared.rpc.project.wiki;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectPageEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdatePageRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdatePageRequest")
public class UpdatePageRequest implements Serializable, IsSerializable {
    DevProjectPageEntity page;
}
