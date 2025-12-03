package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

@Doc("项目构建")
@Table(DevBuildEntity.TABLE_NAME)
@Getter
@Setter
public class DevBuildEntity implements Serializable, IsSerializable {
    public static final String TABLE_NAME = "dev_build";
    public static final String FLD_ID = "id";
    @Name
    @ColDefine(width = 64, notNull = true)
    @Comment("构建ID")
    String id;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 2048, notNull = true)
    @Comment("项目ID")
    String projectId;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 2048, notNull = true)
    @Comment("用户ID")
    Long userId;

    @Comment("创建时间")
    @ColDefine(type = ColType.DATETIME, width = 0)
    @Column(hump = true)
    Timestamp createTime;

    @Comment("结束时间")
    @ColDefine(type = ColType.DATETIME, width = 0)
    @Column(hump = true)
    Timestamp endTime;

    @Comment("状态 0 未开始 1 正在编译 2 错误结束 3 正确结束")
    @Column(hump = true)
    Integer status;

    @Comment("任务工作空间")
    @Column(hump = true)
    String namespace;

    @Comment("任务ID")
    @Column(hump = true)
    String jobId;
}
