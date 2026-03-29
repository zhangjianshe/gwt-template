package cn.mapway.gwt_template.shared.rpc.project.wiki;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * LoadPageRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("LoadPageRequest")
public class LoadPageRequest implements Serializable, IsSerializable {
    String pageId;
}
