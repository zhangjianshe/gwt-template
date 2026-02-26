package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

@Doc("桌面项")
@Table(DesktopItemEntity.TABLE_NAME)
@Getter
@Setter
public class DesktopItemEntity implements Serializable, IsSerializable {
    public static final String TABLE_NAME = "desktop_item";
    public static final String FLD_ID = "id";
    public static final String FLD_USER_ID = "user_id";
    public static final String FLD_RANK = "rank";
    public static final String FLD_SHARE = "share";
    @Name
    @ColDefine(width = 64, notNull = true)
    @Comment("桌面ID")
    String id;

    @Column(hump = true)
    @ColDefine( notNull = true)
    @Comment("用户ID")
    Long userId;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 512, notNull = true)
    @Comment("名称")
    String name;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 1024, notNull = true)
    @Comment("图标")
    String icon;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 1024, notNull = true)
    @Comment("介绍")
    String summary;

    @Comment("创建时间")
    @ColDefine(type = ColType.DATETIME, width = 0)
    @Column(hump = true)
    Timestamp createTime;

    @Comment("类型 0 url 1 MODULE")
    @Column(hump = true)
    Integer kind;

    @Comment("url | module code")
    @Column(hump = true)
    String data;

    @Comment("是否是共享项目")
    @Column(hump = true)
    @Default("false")
    Boolean share;

    @Comment("排序")
    @Column(hump = true)
    @Default("0")
    Integer rank;
}
