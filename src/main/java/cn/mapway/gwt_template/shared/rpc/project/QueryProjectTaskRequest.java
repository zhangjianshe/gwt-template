package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.ApiField;
import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryProjectTaskRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryProjectTaskRequest")
public class QueryProjectTaskRequest implements Serializable, IsSerializable {
    String projectId;
    /**
     * 用户可以自己创建分类,并为分类设定一个名称
     * 该信息存储在 项目表里
     */
    @ApiField(value = "任务的类型 0 任务 1 会议",example = "0")
    Integer catalog;
}
