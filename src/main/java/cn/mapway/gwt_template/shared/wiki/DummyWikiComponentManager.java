package cn.mapway.gwt_template.shared.wiki;

import cn.mapway.gwt_template.client.workspace.wiki.component.EmptyComponent;
import cn.mapway.gwt_template.shared.wiki.component.IWikiComponent;
import cn.mapway.gwt_template.shared.wiki.component.WikiComponentInformation;

import java.util.ArrayList;
import java.util.List;


/**
 * 这是一个模板类，用于编译时生成的模板
 */
public class DummyWikiComponentManager implements WikiComponentManager {
    List<WikiComponentInformation> components;

    public DummyWikiComponentManager() {
        components = new ArrayList<>();
        initComponents();
    }

    protected void initComponents() {
        //这里初始化所有的组件元数据列表

        //__INIT_METADATA_BLOCK__
    }

    @Override
    public IWikiComponent createComponent(String kind) {
        if (kind == null || kind.isEmpty()) {
            return new EmptyComponent();
        }
        //if(kind.equals("wiki")) return new EmptyComponent();
        //if(kind.equals("wiki2")) return new EmptyComponent();

        //__CREATE_BLOCK__

        return new EmptyComponent();
    }

    @Override
    public List<WikiComponentInformation> getComponentsMetadata() {
        return components;
    }

    @Override
    public WikiComponentInformation findComponentMetadata(String kind) {
        WikiComponentInformation component = null;
        for (WikiComponentInformation componentInfo : components) {
            if (kind.equals(componentInfo.getKind())) {
                component = componentInfo;
                break;
            }
        }
        return component;
    }
}
