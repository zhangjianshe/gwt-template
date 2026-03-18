package cn.mapway.gwt_template.client.widget.file;

import lombok.Data;

/**
 * SummaryData
 *
 * @author zhang
 */
@Data
public class SummaryData {
    int queueSize;
    String lastFile;
    long totalSize;
    long hasUploadSize;
    long uploadedSize;
    long speed;
    long remainTime;
    long startTime;

    public SummaryData() {
        this.queueSize = 0;
        this.lastFile = "";
        this.totalSize = 0;
        this.uploadedSize = 0;
        this.speed = 0;
        this.remainTime = 0;
        this.startTime = 0;
        hasUploadSize = 0;
    }
}
