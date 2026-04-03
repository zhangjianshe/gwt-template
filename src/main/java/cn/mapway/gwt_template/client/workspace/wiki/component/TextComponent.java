package cn.mapway.gwt_template.client.workspace.wiki.component;

import cn.mapway.gwt_template.client.workspace.wiki.SectionTypeSelector;
import cn.mapway.gwt_template.shared.db.DevProjectPageSectionEntity;
import cn.mapway.gwt_template.shared.wiki.component.WikiBaseComponent;
import cn.mapway.gwt_template.shared.wiki.component.WikiComponent;
import cn.mapway.gwt_template.shared.wiki.component.WikiComponentInformation;
import cn.mapway.gwt_template.shared.wiki.component.WikiPageContext;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;

@WikiComponent(
        kind = TextComponent.KIND_TEXT,
        name = "文本",
        unicode = Fonts.EME_TEXT,
        summary = "正文文本",
        catalog = "系统",
        alias = "text"
)
public class TextComponent extends WikiBaseComponent {
    public static final String KIND_TEXT = "TEXT";
    private static final TextComponentUiBinder ourUiBinder = GWT.create(TextComponentUiBinder.class);
    @UiField
    HTMLPanel root;
    @UiField
    HTML editor;
    List<String> lines = new ArrayList<>();

    public TextComponent() {
        initWidget(ourUiBinder.createAndBindUi(this));
        setElementEditable(editor.getElement(), true);
        editor.getElement().setAttribute("data-placeholder", "按 '/' 插入组件，或者直接输入内容...");
        editor.addDomHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                // 在 TextComponent 的 KeyDownHandler 中增加
                if (SectionTypeSelector.getDialog(true).isShowing()) {
                    int keyCode = event.getNativeKeyCode();
                    if (keyCode == KeyCodes.KEY_UP || keyCode == KeyCodes.KEY_DOWN) {
                        // 阻止编辑器自身滚动或换行
                        event.preventDefault();
                        // 将按键事件传递给选择器，实现键盘选组件
                        SectionTypeSelector.getDialog(true).getContent().handleNavigation(keyCode);
                        return;
                    }
                    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                        event.preventDefault();
                        // 通知选择器：用户确认了当前高亮的项
                        SectionTypeSelector.getDialog(true).getContent().confirmSelected();
                    }
                } else {
                    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                        // 如果按下了 Shift + Enter，允许浏览器执行默认换行
                        if (event.isShiftKeyDown()) {
                            return;
                        }

                        // 阻止默认行为（不让浏览器在当前 Div 里乱加 <br>）
                        event.preventDefault();
                        event.stopPropagation();

                        // 逻辑：向父容器发送事件，要求在当前组件下方插入一个新的 TextComponent
                        // 这里可以调用 WikiPageContext 的方法
                        if (getContext() != null) {
                            getContext().insertNewComponentAfter(TextComponent.this, KIND_TEXT);
                        }
                    }
                }

            }
        }, KeyDownEvent.getType());
        editor.addDomHandler(event -> {
            String html = editor.getElement().getInnerHTML();
            String text = editor.getElement().getInnerText();

            int slashIndex = text.lastIndexOf("/");
            if (slashIndex != -1) {
                // 提取斜杠后的内容，例如 "/img" 提取出 "img"
                String query = text.substring(slashIndex + 1).trim();

                showComponentSelector(query);
            } else {
                SectionTypeSelector.getDialog(true).hide();
            }
        }, com.google.gwt.event.dom.client.KeyUpEvent.getType());
    }

    private void showComponentSelector(String query) {
        // 1. 获取当前光标相对于窗口的坐标
        NativePoint pos = getCaretGlobalPosition();

        Popup<SectionTypeSelector> dialog = SectionTypeSelector.getDialog(true);
        if (dialog.isShowing()) {
            dialog.setPopupPosition((int) pos.x, (int) pos.y);
            dialog.getContent().load(query);
            return;
        }
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isSelect()) {
                    WikiComponentInformation information = event.getValue();
                    handleCommand(information.getKind());
                    dialog.hide();
                } else if (event.isClose()) {
                    dialog.hide();
                }
            }
        });
        dialog.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {
                dialog.setPopupPosition((int) pos.x, (int) pos.y);
            }
        });
        dialog.getContent().load(query);

    }

    private void handleCommand(String kind) {
        String text = editor.getElement().getInnerText();
        // 找到最后一个斜杠的位置
        int slashIndex = text.lastIndexOf("/");
        if (slashIndex != -1) {
            // 删除从斜杠开始到结尾的所有内容
            String newText = text.substring(0, slashIndex);
            editor.getElement().setInnerText(newText);
        }

        // 2. 检查是否为空，决定是“替换”还是“插入”
        // 注意：innerText 去掉 '/' 后如果是空的，说明该块原本只有这一个字符
        if (StringUtil.isBlank(editor.getElement().getInnerText())) {
            getContext().replaceComponent(this, kind);
        } else {
            getContext().insertNewComponentAfter(this, kind);
        }
    }

    // 这是一个简化的原生 JS 调用，用于获取光标位置
    private native NativePoint getCaretGlobalPosition() /*-{
        var sel = $wnd.getSelection();
        if (sel.rangeCount > 0) {
            var range = sel.getRangeAt(0);
            var rect = range.getBoundingClientRect();
            // 加上页面的滚动偏移量，确保 Popup 定位准确
            return {
                x: rect.left + $wnd.pageXOffset,
                y: rect.bottom + $wnd.pageYOffset
            };
        }
        return {x: 0, y: 0};
    }-*/;

    @Override
    public void initComponent(WikiPageContext context, DevProjectPageSectionEntity section) {
        super.initComponent(context, section);
        lines.clear();
        setChanged(false);
        if (StringUtil.isBlank(section.getContent())) {
            editor.setHTML("");
        } else {
            lines = StringUtil.splitIgnoreBlank(section.getContent(), "\n");
            StringBuilder html = new StringBuilder();
            for (String line : lines) {
                html.append("<div>").append(line).append("</div>");
            }
            editor.setHTML(html.toString());
        }
    }

    @Override
    public Widget getRootWidget() {
        return this;
    }

    @Override
    public void setPlaceholder(String text) {
        editor.getElement().setAttribute("data-placeholder", text);
    }

    @Override
    public DevProjectPageSectionEntity getSection() {
        DevProjectPageSectionEntity section = super.getSection();
        StringBuilder text = new StringBuilder();
        Element element = editor.getElement();

        // 遍历所有子节点（通常是 <div> 或 <br>）
        for (int i = 0; i < element.getChildCount(); i++) {
            com.google.gwt.dom.client.Element child = element.getChild(i).cast();
            String content = child.getInnerText();
            if (content != null) {
                text.append(content).append("\n");
            }
        }

        // 如果没有任何子节点（只有纯文本），直接取 innerText
        if (text.length() == 0) {
            text.append(element.getInnerText());
        }

        String content = text.toString().trim();
        section.setContent(content);
        return section;
    }

    @Override
    public void focus() {
        editor.getElement().focus();
    }

    interface TextComponentUiBinder extends UiBinder<HTMLPanel, TextComponent> {
    }
}