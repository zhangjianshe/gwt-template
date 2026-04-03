package cn.mapway.gwt_template.shared.wiki.component;

import cn.mapway.gwt_template.shared.db.DevProjectPageSectionEntity;
import cn.mapway.ui.shared.HasCommonHandlers;
import com.google.gwt.user.client.ui.Widget;

/**
 * 约束一个WIKI组件的行为
 */
public interface IWikiComponent extends HasCommonHandlers {
    void initComponent(WikiPageContext context, DevProjectPageSectionEntity section);

    Widget getRootWidget();

    boolean isChanged();

    void focus();
}
