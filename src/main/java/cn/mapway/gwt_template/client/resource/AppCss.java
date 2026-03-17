package cn.mapway.gwt_template.client.resource;

import com.google.gwt.resources.client.CssResource;

public interface AppCss extends CssResource {

    /**
     * 对应 .tableHeader
     */
    String tableHeader();

    /**
     * 对应 .tableRow
     */
    String tableRow();

    /**
     * 对应 .success
     */
    String success();

    /**
     * 对应 .secondaryText
     */
    String secondaryText();

    String table();

    String userBox();

    String avatar();

    String userName();

    String userRole();

    String userCard();

    String userCardAvatar();

    String menuItem();

    String menu();

    String menuSeparator();

    String primaryText();

    String mainBackground();

    String menuItemDisabled();

    String boldLink();

    String normalLink();

    @ClassName("popup-header")
    String popupHeader();

    @ClassName("popup-top-panel")
    String popupHeaderPanel();
}