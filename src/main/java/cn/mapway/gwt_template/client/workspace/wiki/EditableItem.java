package cn.mapway.gwt_template.client.workspace.wiki;

import cn.mapway.gwt_template.shared.db.DevProjectPageSectionEntity;
import cn.mapway.gwt_template.shared.doc.SectionKind;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import elemental2.dom.DomGlobal;
import elemental2.dom.Range;
import elemental2.dom.Selection;

public class EditableItem extends CommonEventComposite implements IData<DevProjectPageSectionEntity> {
    private static final EditableItemUiBinder ourUiBinder = GWT.create(EditableItemUiBinder.class);
    @UiField
    HTMLPanel root;
    String oldName = "";
    private DevProjectPageSectionEntity section;

    public EditableItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
        root.getElement().setAttribute("tabindex", "0");
        getElement().setAttribute("contenteditable", "true");
        addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                event.stopPropagation();
            }
        }, MouseDownEvent.getType());
        addDomHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    SectionKind kind = SectionKind.fromInt(section.getKind());
                    switch (kind) {
                        case H1:
                        case H2:
                        case H3:
                        case H4:
                        case H5:
                        case TEXT: {
                            fireEvent(CommonEvent.createEvent(SectionKind.TEXT));
                            event.stopPropagation();
                            event.preventDefault();
                        }
                    }
                }
            }
        }, KeyDownEvent.getType());
    }

    private boolean isCursorAtStart() {
        Selection selection = DomGlobal.window.getSelection();
        if (selection != null && selection.rangeCount > 0) {
            Range range = selection.getRangeAt(0);
            // Returns true if the cursor is at the very first character
            return range.startOffset == 0;
        }
        return false;
    }

    private boolean isCursorAtEnd() {
        Selection selection = DomGlobal.window.getSelection();
        if (selection != null && selection.rangeCount > 0) {
            Range range = selection.getRangeAt(0);

            // In contenteditable, the focusNode is usually the Text node
            if (selection.focusNode != null) {
                int length = 0;
                if (selection.focusNode.nodeType == 3) { // Text Node
                    length = selection.focusNode.nodeValue.length();
                } else {
                    length = selection.focusNode.childNodes.length;
                }
                return range.startOffset >= length;
            }
        }
        return false;
    }

    public void setAbsoluteMode(boolean isAbsolute) {
        Style style = getElement().getStyle();
        if (isAbsolute) {
            style.setPosition(Style.Position.ABSOLUTE);
        } else {
            style.clearPosition();
        }
    }

    public void setPosition(int x, int y) {
        Style style = getElement().getStyle();
        style.setLeft(x, Style.Unit.PX);
        style.setTop(y, Style.Unit.PX);
    }

    public void focus() {
        root.getElement().focus();
    }

    @Override
    public DevProjectPageSectionEntity getData() {
        return section;
    }

    @Override
    public void setData(DevProjectPageSectionEntity pageSection) {
        section = pageSection;

        toUI();
    }

    private void toUI() {
        SectionKind kind = SectionKind.fromInt(section.getKind());
        if (StringUtil.isNotBlank(oldName)) {
            removeStyleName(oldName);
        }
        oldName = kind.getClassName();
        addStyleName(oldName);
        root.getElement().setInnerText(section.getContent());
    }

    interface EditableItemUiBinder extends UiBinder<HTMLPanel, EditableItem> {
    }
}