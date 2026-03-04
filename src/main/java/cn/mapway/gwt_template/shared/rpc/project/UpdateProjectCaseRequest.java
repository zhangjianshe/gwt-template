package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectTaskCaseEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateProjectCaseRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateProjectCaseRequest")
public class UpdateProjectCaseRequest implements Serializable, IsSerializable {
    DevProjectTaskCaseEntity caseEntity;
}
