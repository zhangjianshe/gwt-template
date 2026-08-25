package cn.mapway.gwt_template.client.widget.gridstack;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * GridStack 初始化配置参数的 JsInterop 映射类
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class GridStackOptions {

    /**
     * 网格的列数，默认通常为 12
     */
    @JsProperty
    public int column;

    /**
     * 单个网格单元格的高度。
     * 可以是数字（如 80 表示 80px），也可以是字符串（如 "8rem"、"auto"）
     */
    @JsProperty
    public Object cellHeight;

    /**
     * 模块之间的间距（外边距）。
     * 可以是数字（如 10 表示 10px），或者是用空格分隔的字符串（如 "10px 5px"）
     */
    @JsProperty
    public Object margin;

    /**
     * 是否允许拖拽移动模块，默认为 true
     */
    @JsProperty
    public boolean disableDrag;

    /**
     * 是否允许缩放（调整大小）模块，默认为 true
     */
    @JsProperty
    public boolean disableResize;

    /**
     * 是否允许模块在拖拽时相互叠放（若为 true，则节点不会自动向下挤压换行，而是重叠）
     */
    @JsProperty
    public boolean floatWithGrid;

    /**
     * 拖拽时限制的边界容器，通常设置为 ".grid-stack" 或者是具体的父级选择器
     */
    @JsProperty
    public String dragInOptions;

    /**
     * 动画开关。若为 true，当其他模块被挤开时会带有平滑的平移动画
     */
    @JsProperty
    public boolean animate;

    /**
     * 触发拖拽的手柄选择器。
     * 例如设置为 ".dashboard-item-header"，这样用户只能点住头部拖拽，而不会影响内部图表的交互
     */
    @JsProperty
    public String handle;

    /**
     * 最小行数限制
     */
    @JsProperty
    public int minRow;

    /**
     * 最大行数限制
     */
    @JsProperty
    public int maxRow;
}