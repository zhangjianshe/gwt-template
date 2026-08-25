package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

@Doc("系统日志表")
@Table(SysLogEntity.TABLE_NAME)
@Getter
@Setter
public class SysLogEntity implements Serializable, IsSerializable {
    public static final String TABLE_NAME = "sys_log";
    public static final String FLD_ID = "id";
    public static final String FLD_USER_ID = "userId";
    public static final String FLD_LEVEL = "level";
    public static final String FLD_CONTENT = "content";
    public static final String FLD_ACTION = "action";
    public static final String FLD_CREATE_TIME = "createTime";
    @Name
    @ColDefine(width = 64, notNull = true)
    @Comment("LOG_ID")
    String id;

    @Column(hump = true)
    @ColDefine( notNull = true)
    @Comment("用户ID")
    Long userId;

    @Column(hump = true)
    @ColDefine( notNull = true)
    @Comment("用户名称")
    String userName;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 512, notNull = true)
    @Comment("动作名称")
    String action;

    @Column
    @ColDefine(type = ColType.TEXT,notNull = true)
    @Comment("内容")
    String content;


    @Comment("创建时间")
    @ColDefine(type = ColType.DATETIME, width = 0)
    @Column(hump = true)
    Timestamp createTime;

    @Comment("0 1 2 3")
    @Column(hump = true)
    Integer level;

}
