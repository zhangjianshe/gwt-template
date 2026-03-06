package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.ApiField;
import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryDevWorkspaceRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryDevWorkspaceRequest")
public class QueryDevWorkspaceRequest implements Serializable, IsSerializable {
    @ApiField("是否连带获取目录结构")
    Boolean withFolder = false;

    @ApiField("工作空间ID,如果设置了 就只查询该工作空间")
    String workspaceId;

}
