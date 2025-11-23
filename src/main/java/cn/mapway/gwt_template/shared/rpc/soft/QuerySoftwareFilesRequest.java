package cn.mapway.gwt_template.shared.rpc.soft;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QuerySoftwareFilesRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QuerySoftwareFilesRequest")
public class QuerySoftwareFilesRequest implements Serializable, IsSerializable {
    String softwareId;
}
