package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

@Doc("桌面布局")
@Table(DashboardEntity.TABLE_NAME)
@Getter
@Setter
public class DashboardEntity implements Serializable, IsSerializable {
    public static final String TABLE_NAME = "dashboard";
    public static final String FLD_ID = "id";
    public static final String FLD_USER_ID = "user_id";
    public static final String FLD_RANK = "rank";
    public static final String FLD_NAME = "name";
    @Name
    @ColDefine(width = 64, notNull = true)
    @Comment("layout id")
    String id;

    @Column(hump = true)
    @ColDefine(notNull = true)
    @Comment("用户ID")
    Long userId;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 512, notNull = true)
    @Comment("layout 名称")
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

    @Comment("layout data")
    @Column(hump = true)
    @ColDefine(type = ColType.TEXT)
    @Default("[]")
    String layout;

}
