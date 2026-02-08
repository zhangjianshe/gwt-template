package cn.mapway.gwt_template.client.project.basic;

import cn.mapway.gwt_template.shared.rpc.project.QueryRepoRefsResponse;
import cn.mapway.gwt_template.shared.rpc.project.git.GitRef;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import cn.mapway.ui.client.widget.dialog.Popup;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;

public class ReferenceDropdown extends CommonEventComposite implements IData<QueryRepoRefsResponse>, HasValue<String> {
    private static final ReferenceDropdownUiBinder ourUiBinder = GWT.create(ReferenceDropdownUiBinder.class);
    String value;
    @UiField
    FontIcon fontKind;
    @UiField
    FontIcon drop;
    @UiField
    Label lbName;
    private QueryRepoRefsResponse data;

    public ReferenceDropdown() {
        initWidget(ourUiBinder.createAndBindUi(this));
        drop.setIconUnicode(Fonts.DOWN);
        addDomHandler(event -> {
            popupSelect();
        }, ClickEvent.getType());
    }

    private void popupSelect() {
        Popup<ReferencePanel> popup = ReferencePanel.getPopup(true);
        popup.addCommonHandler(event -> {
            popup.hide();
            if (event.isSelect()) {
                GitRef gitRef = event.getValue();
                setValue(gitRef.getName(), true);
            }
        });
        popup.getContent().setData(data);
        popup.showRelativeTo(this);
    }

    @Override
    public QueryRepoRefsResponse getData() {
        return data;
    }

    @Override
    public void setData(QueryRepoRefsResponse obj) {
        data = obj;
        setValue(obj.getDefaultBranch(), true);
    }


    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        setValue(value, false);
    }

    @Override
    public void setValue(String value, boolean fireEvents) {
        this.value = value;
        toUI();
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    private void toUI() {
        GitRef gitRef = findGitRef(value);
        if (gitRef != null) {
            if (gitRef.getKind().equals(0)) {
                fontKind.setIconUnicode(Fonts.BRANCH);
            } else if (gitRef.getKind().equals(1)) {
                fontKind.setIconUnicode(Fonts.LABEL);
            }
        }
        lbName.setText(value);
    }

    private GitRef findGitRef(String value) {
        for (GitRef ref : data.getRefs()) {
            if (ref.getName().equals(value)) {
                return ref;
            }
        }
        return null;
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    interface ReferenceDropdownUiBinder extends UiBinder<HTMLPanel, ReferenceDropdown> {
    }
}