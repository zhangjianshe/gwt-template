package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;

/**
 * 项目分组成员关联表
 * 建立 团队分组 与 用户 之间的多对多关系
 */
@Doc("项目分组成员")
@Table(DevProjectTeamMemberEntity.TBL_DEV_PROJECT_TEAM_MEMBER)
@Comment("项目分组成员")
@Getter
@Setter
@PK(value = {"teamId", "userId"})
public class DevProjectTeamMemberEntity implements Serializable, IsSerializable {

    /* 表名常量 */
    public static final String TBL_DEV_PROJECT_TEAM_MEMBER = "dev_project_team_member";

    /* 字段名常量 */
    public static final String FLD_TEAM_ID = "team_id";
    public static final String FLD_USER_ID = "user_id";
    public static final String FLD_SUMMARY = "summary";
    public static final String FLD_PERMISSION = "permission";

    @Column(FLD_TEAM_ID)
    @Comment("分组ID")
    @ColDefine(width = 64, notNull = true)
    String teamId;

    @Column(FLD_USER_ID)
    @Comment("成员ID")
    Long userId;

    @Column(FLD_SUMMARY)
    @Comment("成员在组内的简介或备注")
    @ColDefine(width = 256, notNull = true)
    String summary;

    @Column(FLD_PERMISSION)
    @Comment("成员在组内的权限级别")
    @Default("0")
    Integer permission;

}