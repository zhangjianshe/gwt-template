package cn.mapway.gwt_template.client.workspace.events;

import com.google.gwt.event.dom.client.*;
import elemental2.dom.WheelEvent;

public interface IMouseHandler {
    void onMouseDown(MouseDownEvent event);

    void onMouseUp(MouseUpEvent event);

    void onMouseMove(MouseMoveEvent event);

    void start(GanttHitResult hitResult, MouseDownEvent event);

    default void onKeyDown(KeyDownEvent event) {
    }

    default void onMouseWheel(WheelEvent event) {
    }


    default void onDoubleClick(DoubleClickEvent event) {
    }

}
