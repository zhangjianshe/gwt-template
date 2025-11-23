package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

@Doc("系统软件定义")
@Table(SysSoftwareEntity.TABLE_NAME)
@Getter
@Setter
public class SysSoftwareEntity implements Serializable, IsSerializable {
    public static final String TABLE_NAME = "sys_software";
    public static final String FLD_ID = "id";
    public static final String FLD_CREATE_TIME = "create_time";
    public static final String FLD_TOKEN = "token";
    @Name
    @ColDefine(width = 128, notNull = true)
    @Comment("ID")
    String id;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 256, notNull = true)
    @Comment("名称")
    String name;


    @Column
    @ColDefine(type = ColType.VARCHAR, width = 512, notNull = true)
    @Comment("logo")
    String logo;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 512, notNull = true)
    @Comment("summary")
    String summary;

    @Comment("创建时间")
    @ColDefine(type = ColType.DATETIME, width = 0)
    @Column(hump = true)
    Timestamp createTime;

    @Column
    @Comment("访问TOKEN")
    String token;

    @Column("user_id")
    @Comment("用户ID")
    @Default("0")
    Long userId;

}
