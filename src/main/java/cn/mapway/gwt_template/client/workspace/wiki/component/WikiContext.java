package cn.mapway.gwt_template.client.workspace.wiki.component;

import cn.mapway.gwt_template.shared.wiki.WikiComponentManager;
import com.google.gwt.core.client.GWT;

public class WikiContext {
    private static WikiComponentManager instance;

    public static WikiComponentManager get() {
        if (instance == null) {
            instance = GWT.create(WikiComponentManager.class);
        }
        return instance;
    }
}