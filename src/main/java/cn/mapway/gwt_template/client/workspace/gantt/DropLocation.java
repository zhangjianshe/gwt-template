package cn.mapway.gwt_template.client.workspace.gantt;

import cn.mapway.ui.client.mvc.Size;
import lombok.Getter;
import lombok.Setter;

public class DropLocation {
    public GanttItem sourceItem; // 参照物
    public GanttItem targetItem; // 参照物
    public GanttDropPosition position;
    @Getter
    public Size mousePosition=new Size(0,0);
    @Getter
    @Setter
    private boolean valid = false;
}
