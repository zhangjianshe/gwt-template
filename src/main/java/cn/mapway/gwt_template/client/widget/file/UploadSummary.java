package cn.mapway.gwt_template.client.widget.file;

import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * UploadSummary
 * 上传统计信息
 *
 * @author zhang
 */
public class UploadSummary extends Composite implements IData<SummaryData> {
    private static final UploadSummaryUiBinder ourUiBinder = GWT.create(UploadSummaryUiBinder.class);
    SummaryData summaryData;
    @UiField
    Label lbQueueSize;
    @UiField
    Label lbTotal;
    @UiField
    Label lbFinished;
    @UiField
    Label lbElapsed;
    @UiField
    Label lbRemaining;
    @UiField
    Label lbSpeed;

    public UploadSummary() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public SummaryData getData() {
        return summaryData;
    }

    @Override
    public void setData(SummaryData obj) {
        summaryData = obj;
        toUI();
    }

    private void toUI() {
        //用时 秒
        long elapsed = (System.currentTimeMillis() - summaryData.getStartTime()) / 1000;
        long loaded = (summaryData.getHasUploadSize() + summaryData.getUploadedSize());
        double speed = 0.0d;
        if (elapsed > 0) {
            speed = loaded / (1024.0 * elapsed);
            if (speed < 1024) {
                lbSpeed.setText(StringUtil.formatDouble(speed, 1) + "KB/s");
            } else if (speed < 1024 * 1024) {
                lbSpeed.setText(StringUtil.formatDouble(speed / 1024, 1) + "MB/s");
            } else {
                lbSpeed.setText(StringUtil.formatDouble(speed / 1024 / 1024, 1) + "GB/s");
            }
        } else {
            lbSpeed.setText("0KB/s");
        }
        if (speed > 0) {
            lbRemaining.setText(StringUtil.formatTimeSpan((long) ((summaryData.getTotalSize() - loaded) / (1024 * speed))));
        } else {
            lbRemaining.setText("--");
        }

        lbElapsed.setText(StringUtil.formatTimeSpan(elapsed));
        lbQueueSize.setText(summaryData.getQueueSize() + "个文件");
        lbTotal.setText(StringUtil.formatFileSize(summaryData.getTotalSize()));
        lbFinished.setText(StringUtil.formatFileSize(loaded));
    }

    interface UploadSummaryUiBinder extends UiBinder<ScrollPanel, UploadSummary> {
    }
}
