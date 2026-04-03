package cn.mapway.gwt_template.shared.wiki.component;

/**
 * 用于描述一个组件
 */
public @interface WikiComponent {
    /**
     * 名称　用于展示
     * @return
     */
    String name();

    /**
     * 图标　用于展示
     * @return
     */
    String unicode();

    /**
     * 类型　用于数据存储
     * @return
     */
    String kind();

    String summary();

    String catalog();

}
