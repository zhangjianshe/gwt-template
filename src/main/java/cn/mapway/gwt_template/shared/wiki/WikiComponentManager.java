package cn.mapway.gwt_template.shared.wiki;


import cn.mapway.gwt_template.shared.wiki.component.IWikiComponent;
import cn.mapway.gwt_template.shared.wiki.component.WikiComponentInformation;

import java.util.List;

/**
 * 页面组件管理接口
 */
public interface WikiComponentManager {
    IWikiComponent createComponent(String kind);
    List<WikiComponentInformation> getComponentsMetadata();
    WikiComponentInformation findComponentMetadata(String kind);
}
