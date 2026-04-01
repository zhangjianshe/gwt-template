package cn.mapway.gwt_template.client.workspace.wiki;

import cn.mapway.gwt_template.shared.db.DevProjectPageSectionEntity;
import cn.mapway.gwt_template.shared.doc.SectionKind;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.PlusButton;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTMLPanel;

public class EditableItem extends CommonEventComposite implements IData<DevProjectPageSectionEntity>, IItem {
    private static final EditableItemUiBinder ourUiBinder = GWT.create(EditableItemUiBinder.class);
    @UiField
    HTMLPanel root;
    @UiField
    HTMLPanel operator;
    @UiField
    HTMLPanel box;
    @UiField
    PlusButton btnAdd;
    String oldName = "";
    private DevProjectPageSectionEntity section;

    public EditableItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
        root.getElement().setAttribute("tabindex", "0");
        root.getElement().setAttribute("contenteditable", "true");
        root.addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                event.stopPropagation();
            }
        }, MouseDownEvent.getType());
        root.addDomHandler(new KeyDownHandler() {
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

        box.addDomHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                operator.setVisible(true);
            }
        }, MouseOverEvent.getType());
        box.addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                operator.setVisible(false);
            }
        }, MouseOutEvent.getType());
    }


    public void focus() {
        root.getElement().focus();
    }

    @Override
    public DevProjectPageSectionEntity getData() {
        fromUI();
        return section;
    }

    private void fromUI() {
        section.setContent(root.getElement().getInnerText());
    }

    String oldValue = "";
    @Override
    public void setData(DevProjectPageSectionEntity pageSection) {
        section = pageSection;
        oldValue=section.getContent();
        toUI();
    }

    private void toUI() {
        SectionKind kind = SectionKind.fromInt(section.getKind());
        if (StringUtil.isNotBlank(oldName)) {
            root.removeStyleName(oldName);
        }
        oldName = kind.getClassName();
        root.addStyleName(oldName);
        root.getElement().setInnerText(section.getContent());
    }

    public void setPlaceHolder(String placeholder) {
        root.getElement().setPropertyString("placeholder", placeholder);
    }

    @UiHandler("btnAdd")
    public void btnAddClick(ClickEvent event) {
        //创建和自己一样的
        fireEvent(CommonEvent.createEvent(SectionKind.fromInt(section.getKind())));
    }

    @Override
    public boolean isChanged() {
        String newText = root.getElement().getInnerText();
        return StringUtil.isNotBlank(newText) && !newText.equals(oldValue);
    }

    interface EditableItemUiBinder extends UiBinder<HTMLPanel, EditableItem> {
    }
}