package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 项目关联的代码仓库
 * 用于描述项目与多个代码库之间的多对多或一对多关系
 */
@Doc("项目关联的代码仓库")
@Table(DevProjectRepoEntity.TBL_DEV_PROJECT_REPO)
@Comment("项目关联的代码仓库")
@Getter
@Setter
@PK(value = {"projectId", "repositoryId"})
public class DevProjectRepoEntity implements Serializable, IsSerializable {

    /* 表名常量 */
    public static final String TBL_DEV_PROJECT_REPO = "dev_project_repo";

    /* 字段名常量 */
    public static final String FLD_PROJECT_ID = "project_id";
    public static final String FLD_REPOSITORY_ID = "repository_id";
    public static final String FLD_CREATE_TIME = "create_time";

    @Column(FLD_PROJECT_ID)
    @Comment("项目ID")
    @ColDefine(width = 64, notNull = true)
    String projectId;

    @Column(FLD_REPOSITORY_ID)
    @Comment("仓库ID")
    @ColDefine(width = 64, notNull = true)
    String repositoryId;

    @Column(FLD_CREATE_TIME)
    @Comment("创建时间")
    @ColDefine(notNull = true)
    Timestamp createTime;

}