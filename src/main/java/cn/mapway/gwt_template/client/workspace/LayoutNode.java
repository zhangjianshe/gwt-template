package cn.mapway.gwt_template.client.workspace;

import cn.mapway.gwt_template.shared.db.DevProjectTeamEntity;
import elemental2.dom.HTMLImageElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LayoutNode {
    DevProjectTeamEntity data;
    double x, y;
    HTMLImageElement chargeImage;
    boolean isSelected = false;
}
