package cn.mapway.gwt_template.shared.db;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 页面的提交清单
 */
@Getter
@Setter
public class PageManifest implements Serializable, IsSerializable {
    List<SectionIndex> sections;
}
