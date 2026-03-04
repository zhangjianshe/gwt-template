package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

@Doc("GIT 仓库表")
@Table(DevRepositoryEntity.TABLE_NAME)
@Getter
@Setter
public class DevRepositoryEntity implements Serializable, IsSerializable {
    public static final String TABLE_NAME = "dev_repository";
    public static final String FLD_ID = "id";
    public static final String FLD_OWNER_NAME = "owner_name";
    public static final String FLD_FULL_NAME = "full_name";
    public static final String FLD_NAME = "name";
    public static final String FLD_USER_ID = "user_id";
    public static final String FLD_STATUS = "status";
    @Name
    @ColDefine(width = 64, notNull = true)
    @Comment("项目ID")
    String id;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 64, notNull = true)
    @Comment("项目代码")
    String name;

    @Column(hump = true)
    @ColDefine(type = ColType.VARCHAR, width = 512)
    @Comment("项目全程")
    String fullName;

    @Comment("创建时间")
    @ColDefine(type = ColType.DATETIME, width = 0)
    @Column(hump = true)
    Timestamp createTime;

    @Comment("创建用户")
    @Column(hump = true)
    Long userId;

    /**
     * 系统中用户name和 groupName全局唯一
     */
    @Comment("拥有者名称")
    @Column(hump = true)
    String ownerName;

    @Comment("源代码")
    @ColDefine(type = ColType.VARCHAR, width = 512)
    @Column(hump = true)
    String sourceUrl;

    @Comment("build script")
    @ColDefine(type = ColType.TEXT)
    @Column(hump = true)
    String buildScript;

    @Comment("deploy server id")
    @ColDefine(type = ColType.VARCHAR, width = 64)
    @Column(hump = true)
    String deployServer;

    @Comment("serviceUrl")
    @ColDefine(type = ColType.VARCHAR, width = 512)
    @Column(hump = true)
    String serviceUrl;

    @Comment("拥有者类型")
    @Column("owner_kind")
    @Default("0")
    Integer ownerKind;

    @Comment("项目简要介绍")
    @Column("summary")
    @Default("")
    @ColDefine(type = ColType.VARCHAR, width = 1024)
    String summary;

    @Comment("成员数量")
    @Column(hump = true)
    @Default("1")
    Integer memberCount;

    @Comment("项目标签,号分割")
    @Column(hump = true)
    @Default("")
    String tags;

    @Comment("是否公开")
    @Column(hump = true)
    @Default("false")
    Boolean isPublic;

    @Comment("项目状态参见　ProjectStatus枚举")
    @Column(hump = true)
    @Default("1")
    Integer status;

    @Comment("最后的消息")
    @Column(hump = true)
    @Default("")
    String lastMessage;
}
