package cn.mapway.gwt_template.shared.db;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 开发组实体
 */
@Table(DevMyProjectEntity.TABLE_NAME)
@Getter
@Setter
public class DevGroupEntity implements Serializable, IsSerializable {
    public static final String FLD_NAME = "name";
    public static final String FLD_USERID = "userId";
    @Name
    @Comment("分组名称")
    @ColDefine(type = ColType.VARCHAR,width = 56, notNull = true)
    String name;

    @Column(hump = true)
    @Comment("分组全名")
    @ColDefine(type = ColType.VARCHAR,width = 128, notNull = true)
    String fullName;

    @Column(hump = true)
    @Comment("创建人ID")
    @ColDefine(notNull = true)
    Long userId;

    @Column(hump = true)
    @Comment("组成员数量")
    @Default("1")
    Integer memberCount;

    @Column(hump = true)
    @Comment("创建时间")
    Timestamp createTime;

    @Column(hump = true)
    @Comment("分组图像")
    @ColDefine(type = ColType.VARCHAR, width = 256)
    String icon;

    @Column(hump = true)
    @Comment("EMAIl")
    @ColDefine(type = ColType.VARCHAR, width = 256)
    String email;

    @Column(hump = true)
    @Comment("网址")
    @ColDefine(type = ColType.VARCHAR, width = 512)
    String website;

    @Column(hump = true)
    @Comment("地址")
    @ColDefine(type = ColType.VARCHAR, width = 512)
    String address;

    
}
