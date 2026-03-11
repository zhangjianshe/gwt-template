package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectMember;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目管理成员分组
 * 构成一个项目的组织结构图 (Team/Group Hierarchy)
 */
@Doc("项目成员分组")
@Table(DevProjectTeamEntity.TBL_DEV_PROJECT_TEAM)
@Comment("项目成员分组")
@Getter
@Setter
public class DevProjectTeamEntity implements Serializable, IsSerializable {

    /* 表名常量 */
    public static final String TBL_DEV_PROJECT_TEAM = "dev_project_team";

    /* 字段名常量 */
    public static final String FLD_ID = "id";
    public static final String FLD_PARENT_ID = "parent_id";
    public static final String FLD_PROJECT_ID = "project_id";
    public static final String FLD_NAME = "name";
    public static final String FLD_CREATE_TIME = "create_time";
    public static final String FLD_COLOR = "color";
    public static final String FLD_UNICODE = "unicode";
    public static final String FLD_ICON = "icon";
    public static final String FLD_SUMMARY = "summary";
    public static final String FLD_CHARGER = "charger";
    public static final String FLD_TEAM_PERMISSION = "team_permission";

    @Name
    @Column(FLD_ID)
    @Comment("分组ID")
    @ColDefine(width = 64, notNull = true)
    String id;

    @Column(FLD_PARENT_ID)
    @Comment("分组的父ID")
    @ColDefine(width = 64)
    String parentId;

    @Column(FLD_PROJECT_ID)
    @Comment("项目ID")
    @ColDefine(width = 64, notNull = true)
    String projectId;

    @Column(FLD_NAME)
    @Comment("名称")
    @ColDefine(width = 128, notNull = true)
    String name;

    @Column(FLD_CREATE_TIME)
    @Comment("创建时间")
    @ColDefine(notNull = true)
    Timestamp createTime;

    @Column(FLD_COLOR)
    @Comment("分组主颜色")
    @ColDefine(width = 64, notNull = true)
    String color;

    @Column(FLD_UNICODE)
    @Comment("分组字体图标")
    @ColDefine(width = 16)
    String unicode;

    @Column(FLD_ICON)
    @Comment("分组图标")
    @ColDefine(width = 256)
    String icon;

    @Column(FLD_SUMMARY)
    @Comment("介绍")
    @ColDefine(width = 1024, notNull = true)
    String summary;

    @Column(FLD_CHARGER)
    @Comment("分组组长")
    Long charger;

    @Column(FLD_TEAM_PERMISSION)
    @Comment("小组权限")
    Integer teamPermission;

    private List<DevProjectTeamEntity> children = new ArrayList<>();
    private List<ProjectMember> members = new ArrayList<>();
}