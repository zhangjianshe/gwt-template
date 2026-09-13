package cn.mapway.gwt_template.shared.rpc.soft;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/** Current state of a resumable software upload. */
@Data
@Doc("UploadSoftwareFile2Response")
public class UploadSoftwareFile2Response implements Serializable, IsSerializable {
    String uploadId;
    long chunkSize;
    long totalSize;
    long receivedSize;
    boolean completed;
    String url;
}
