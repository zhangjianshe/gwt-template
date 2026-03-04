package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * DeleteProjectCaseRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("DeleteProjectCaseRequest")
public class DeleteProjectCaseRequest implements Serializable, IsSerializable {
    String caseId;
}
