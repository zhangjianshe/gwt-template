package cn.mapway.gwt_template.client.docker;

import cn.mapway.gwt_template.shared.rpc.file.FileUtil;
import cn.mapway.gwt_template.shared.rpc.project.module.ResItem;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

public class FileItem extends CommonEventComposite implements IData<ResItem> {
    private static final FileItemUiBinder ourUiBinder = GWT.create(FileItemUiBinder.class);
    @UiField
    Label lbName;
    @UiField
    FontIcon icon;
    @UiField
    Label lbSize;
    private ResItem data;

    public FileItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
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
        if (!data.getIsDir()) {
            icon.setIconUnicode(FileUtil.iconFromSuffix(StringUtil.suffix(data.getPathName())));
            lbSize.setText(StringUtil.formatFileSize(data.getFileSize().longValue()));
        } else {
            icon.setIconUnicode(Fonts.FOLDER);
            lbSize.setText("");
        }
    }


    interface FileItemUiBinder extends UiBinder<HTMLPanel, FileItem> {
    }
}