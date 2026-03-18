package cn.mapway.gwt_template.client.workspace.res;

import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.db.DevProjectResourceEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NavInfo {
    DevProjectEntity project;
    DevProjectResourceEntity resource;
    String relPath;
    private String file;
}
