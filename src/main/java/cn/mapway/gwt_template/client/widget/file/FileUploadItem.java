package cn.mapway.gwt_template.client.widget.file;

import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.DeleteButton;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import elemental2.dom.File;

/**
 * FileUploadItem
 * 文件上传项
 *
 * @author zhang
 */
public class FileUploadItem extends CommonEventComposite {
    private static final FileUploadItemUiBinder ourUiBinder = GWT.create(FileUploadItemUiBinder.class);
    @UiField
    ProgressLabel lbName;
    @UiField
    Label lbSize;
    @UiField
    DeleteButton btnDelete;
    @UiField
    ProgressLabel lbProgress;
    long uploadSize = 0;
    long totalSize = 0;
    private File file;

    public FileUploadItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        lbName.setText(file.name);
        lbSize.setText(StringUtil.formatFileSize((long) file.size));
        totalSize = file.size;
    }

    public void setUploadSize(long uploadSize) {
        this.uploadSize = uploadSize;
        updateUI();
    }

    public boolean isFinished() {
        return uploadSize >= totalSize;
    }

    /**
     * 更新UI
     */
    private void updateUI() {
        if (totalSize > 0) {
            if (uploadSize >= totalSize) {
                btnDelete.setIconUnicode(Fonts.DONE);
            } else {
                int progress=(int) (uploadSize * 100.0 / totalSize);
                lbProgress.setProgress(progress);
            }
        } else {
            lbProgress.setProgress(0);
        }
    }

    @UiHandler("btnDelete")
    public void btnDeleteClick(ClickEvent event) {
        if (!isFinished()) {
            fireEvent(CommonEvent.deleteEvent(event));
        }
    }

    interface FileUploadItemUiBinder extends UiBinder<HTMLPanel, FileUploadItem> {
    }
}
