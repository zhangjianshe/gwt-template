package cn.mapway.gwt_template.shared.rpc.project.wiki;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectPageEntity;
import cn.mapway.gwt_template.shared.db.DevProjectPageSectionEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * LoadPageResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("LoadPageResponse")
public class LoadPageResponse implements Serializable, IsSerializable {
    DevProjectPageEntity page;
    List<DevProjectPageSectionEntity> sections;
}
