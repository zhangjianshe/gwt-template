package cn.mapway.gwt_template.shared.rpc.docker;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * QueryDockerAppInfoResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryDockerAppInfoResponse")
public class QueryDockerAppInfoResponse implements Serializable, IsSerializable {
    String totalSize;
    String status;
    List<String> errors;
    List<String> services;

    public QueryDockerAppInfoResponse() {
        errors = new ArrayList<>();
        services = new ArrayList<>();
    }
}
