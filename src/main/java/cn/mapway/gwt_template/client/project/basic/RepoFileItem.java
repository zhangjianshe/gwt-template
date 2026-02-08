package cn.mapway.gwt_template.client.project.basic;

import cn.mapway.gwt_template.shared.rpc.project.RepoItem;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

public class RepoFileItem extends CommonEventComposite implements IData<RepoItem> {
    private static final RepoFileItemUiBinder ourUiBinder = GWT.create(RepoFileItemUiBinder.class);
    @UiField
    Label lbName;
    @UiField
    Label lbSummary;
    @UiField
    Label lbDate;
    @UiField
    FontIcon icon;
    @UiField
    Label lbSize;
    private RepoItem data;

    public RepoFileItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public RepoItem getData() {
        return data;
    }

    @Override
    public void setData(RepoItem obj) {
        data = obj;
        toUI();
    }

    private void toUI() {
        lbName.setText(StringUtil.extractName(data.getName()));
        lbDate.setText(StringUtil.toRelativeTime(data.getDate()));
        lbSummary.setText(data.getSummary());
        if (data.isDir()) {
            icon.setIconUnicode(Fonts.FOLDER);
        } else {
            icon.setIconUnicode(Fonts.FILE);
            lbSize.setText(StringUtil.formatFileSize(data.getSize()));
        }
    }



    @UiHandler("lbName")
    public void lbNameClick(ClickEvent event) {
        fireEvent(CommonEvent.selectEvent(data));
    }

    interface RepoFileItemUiBinder extends UiBinder<HTMLPanel, RepoFileItem> {
    }
}