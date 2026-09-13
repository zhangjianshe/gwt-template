package cn.mapway.gwt_template.shared.rpc.soft;

import cn.mapway.document.annotation.Doc;
import lombok.Data;

/** Metadata used to initialize a resumable software upload. */
@Data
@Doc("UploadSoftwareFile2Request")
public class UploadSoftwareFile2Request {
    String token;
    String version;
    String name;
    String fileName;
    String summary;
    String os;
    String arch;
    long totalSize;
    String sha256;
}
