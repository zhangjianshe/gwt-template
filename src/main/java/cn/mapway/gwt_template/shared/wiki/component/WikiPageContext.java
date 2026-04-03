package cn.mapway.gwt_template.shared.wiki.component;

import cn.mapway.gwt_template.client.workspace.wiki.component.WikiContext;
import cn.mapway.gwt_template.shared.db.DevProjectPageEntity;
import cn.mapway.gwt_template.shared.db.DevProjectPageSectionEntity;
import com.google.gwt.user.client.ui.FlowPanel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class WikiPageContext {
    DevProjectPageEntity page;
    List<IWikiComponent> sections;
    FlowPanel container;

    public WikiPageContext(FlowPanel page) {
        sections = new ArrayList<>();
        this.container = page;
    }

    public void insertNewComponentAfter(IWikiComponent currentComponent, String kind) {
        int index = container.getWidgetIndex(currentComponent.getRootWidget());
        if (index == -1) return;

        // 1. 创建新组件数据实体
        DevProjectPageSectionEntity newSection = new DevProjectPageSectionEntity();
        newSection.setPageId(page.getId());
        newSection.setKind(kind);
        newSection.setContent(""); // 初始内容为空
        // 如果你有排序字段（如 sortIndex），在这里计算 index + 1 的值

        // 2. 通过 Manager 创建组件实例
        IWikiComponent newComponent = WikiContext.get().createComponent(kind);
        newComponent.initComponent(this, newSection);

        // 3. 同步更新 sections 列表和 UI 容器
        sections.add(index + 1, newComponent);
        container.insert(newComponent.getRootWidget(), index + 1);

        // 4. 自动聚焦到新组件
        newComponent.focus();
    }

    public void replaceComponent(IWikiComponent oldComp, String newKind) {
        int index = container.getWidgetIndex(oldComp.getRootWidget());

        IWikiComponent newComp = WikiContext.get().createComponent(newKind);
        newComp.initComponent(this, new DevProjectPageSectionEntity());

        sections.set(index, newComp);
        container.remove(index);
        container.insert(newComp.getRootWidget(), index);

        newComp.focus();
    }

    public void resetContext(DevProjectPageEntity pageEntity) {
        page = pageEntity;
        sections.clear();
    }
    public void removeComponent(IWikiComponent component) {
        if (sections.size() <= 1) return; // 至少保留一个块

        int index = container.getWidgetIndex(component.getRootWidget());
        container.remove(index);
        sections.remove(component);

        // 聚焦到上一个块
        if (index > 0) {
            sections.get(index - 1).focus();
        }
    }
}
