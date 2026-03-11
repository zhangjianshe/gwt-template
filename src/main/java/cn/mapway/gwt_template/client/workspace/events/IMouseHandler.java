package cn.mapway.gwt_template.client.workspace.events;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;

public interface IMouseHandler {
    void onMouseDown(MouseDownEvent event);
    void onMouseUp(MouseUpEvent event);
    void onMouseMove(MouseMoveEvent event);
    void start(GanttHitResult hitResult, MouseDownEvent event);
    default void onKeyDown(KeyDownEvent event) {};
}
