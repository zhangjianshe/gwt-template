package cn.mapway.gwt_template.client.workspace.task;

import cn.mapway.gwt_template.shared.rpc.file.FileUtil;
import cn.mapway.gwt_template.shared.rpc.project.module.ResItem;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import cn.mapway.ui.client.widget.buttons.DeleteButton;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

public class AttachItem extends CommonEventComposite implements IData<ResItem> {
    private static final AttachItemUiBinder ourUiBinder = GWT.create(AttachItemUiBinder.class);
    @UiField
    DeleteButton btnDelete;
    @UiField
    Label lbName;
    @UiField
    FontIcon icon;
    private ResItem data;

    public AttachItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
        addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                event.preventDefault();
                fireEvent(CommonEvent.selectEvent(data));
            }
        }, ClickEvent.getType());
    }

    @Override
    public ResItem getData() {
        return data;
    }

    @Override
    public void setData(ResItem obj) {
        data = obj;
        toUI();
    }

    private void toUI() {
        lbName.setText(data.getPathName());
        icon.setIconUnicode(FileUtil.iconFromSuffix(StringUtil.suffix(data.getPathName())));
    }

    @UiHandler("btnDelete")
    public void btnDeleteClick(ClickEvent event) {
        fireEvent(CommonEvent.deleteEvent(data));
    }

    public void enableEditable(boolean editable) {
        btnDelete.setEnabled(editable);
    }

    interface AttachItemUiBinder extends UiBinder<HTMLPanel, AttachItem> {
    }
}