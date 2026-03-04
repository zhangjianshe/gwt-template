package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectTaskCaseEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateProjectCaseResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateProjectCaseResponse")
public class UpdateProjectCaseResponse implements Serializable, IsSerializable {
    DevProjectTaskCaseEntity caseEntity;
}
