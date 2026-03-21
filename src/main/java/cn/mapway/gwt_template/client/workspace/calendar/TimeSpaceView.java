package cn.mapway.gwt_template.client.workspace.calendar;

import cn.mapway.ui.client.mvc.Rect;
import cn.mapway.ui.client.mvc.Size;
import lombok.Getter;
import lombok.Setter;

import static cn.mapway.gwt_template.client.workspace.gantt.CalendarTimes.MS_PER_DAY;

/**
 * 坐标转换工具
 * 核心逻辑：
 * 1. World Space: X 轴代表从原点开始的毫秒数 (ms)，Y 轴代表纵向偏移 (px)
 * 2. View Space: 屏幕上的像素位置
 */
public class TimeSpaceView {
    // 视图在世界坐标系中的起始点 (x 为时间毫秒，y 为纵向像素)
    @Getter
    private final Rect viewRect;

    // 每单位(天)占用的像素数，即缩放比例
    @Setter
    private double dayWidth = 100.0;

    public TimeSpaceView() {
        viewRect = new Rect(0, 0, 0, 0);
    }

    /**
     * 设置当前视图滚动的到的位置
     *
     * @param timeMs  世界坐标 X (时间戳)
     * @param offsetY 世界坐标 Y (像素)
     */
    public void setViewOffset(double timeMs, double offsetY) {
        viewRect.setX(timeMs);
        viewRect.setY(offsetY);
    }

    /**
     * 设置当前视图滚动的到的位置
     *
     * @param worldWidth  世界坐标 X (时间戳)
     * @param worldHeight 世界坐标 Y (像素)
     */
    public void setViewSize(double worldWidth, double worldHeight) {
        viewRect.setWidth(worldWidth);
        viewRect.setHeight(worldHeight);
    }

    /**
     * 毫秒转像素比例
     */
    private double getPxPerMs() {
        return dayWidth / MS_PER_DAY;
    }

    /**
     * 投影：将世界坐标（时间，垂直位移）转换为屏幕像素坐标
     * screenX = (worldTime - viewStartTime) * (dayWidth / MS_PER_DAY)
     */
    public void project(Size worldPosition, Size screenPosition) {
        double pxPerMs = getPxPerMs();
        screenPosition.x = (worldPosition.x - viewRect.x) * pxPerMs;
        screenPosition.y = worldPosition.y - viewRect.y;
    }

    /**
     * 反投影：将屏幕像素坐标转换为世界坐标（时间戳，垂直位移）
     * worldTime = viewStartTime + (screenX / (dayWidth / MS_PER_DAY))
     */
    public void unProject(Size screenPosition, Size worldPosition) {
        double pxPerMs = getPxPerMs();
        worldPosition.x = viewRect.x + (screenPosition.x / pxPerMs);
        worldPosition.y = viewRect.y + screenPosition.y;
    }

    /**
     * 辅助方法：仅获取长度转换（不带位移）
     */
    public double durationToPx(double durationMs) {
        return durationMs * getPxPerMs();
    }

    public double pxToDuration(double pixelSpan) {
        return (pixelSpan / dayWidth) * MS_PER_DAY;
    }

    public void projectRect(Rect worldRect, Rect screenRect) {
        // 转换起点
        double pxPerMs = getPxPerMs();
        screenRect.x = (worldRect.x - viewRect.x) * pxPerMs;
        screenRect.y = worldRect.y - viewRect.y;

        // 转换尺寸：宽度是时间跨度转像素，高度通常是 1:1 像素映射
        screenRect.width = durationToPx(worldRect.width); // 修正：传入 worldRect 的宽度
        screenRect.height = worldRect.height;
    }
}