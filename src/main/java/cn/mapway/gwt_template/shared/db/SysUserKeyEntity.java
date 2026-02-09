package cn.mapway.gwt_template.shared.db;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

import static cn.mapway.gwt_template.shared.db.SysUserKeyEntity.TABLE_NAME;

/**
 * 用户的public key list
 * id 存储一个公钥的指纹　用于进行公钥的索引查询
 */
@Table(TABLE_NAME)
@Getter
@Setter
public class SysUserKeyEntity implements Serializable, IsSerializable {
    public final static String TABLE_NAME = "sys_user_key";
    public final static String FLD_ID = "id";
    public final static String FLD_KEY = "key";
    public final static String FLD_USER_ID = "user_id";
    public final static String FLD_NAME = "name";
    public final static String FLD_USER_NAME = "user_name";
    public static final String FLD_CREATE_TIME = "create_time";
    public static final String FLD_LAST_USED = "last_used";
    public static final String FLD_ACTION = "action";

    @Name
    @ColDefine(type = ColType.VARCHAR, width = 64)
    String id;

    @ColDefine(type = ColType.TEXT, notNull = true)
    String key;

    @Column(hump = true)
    @ColDefine(notNull = true)
    Long userId;

    @Column(hump = true)
    @ColDefine(notNull = true)
    String userName;

    @Column
    @Default("")
    String name;

    @Column(hump = true)
    @ColDefine(notNull = true)
    Timestamp createTime;

    @Column(hump = true)
    @ColDefine(notNull = true)
    Long expiredTime;

    @Column(hump = true)
    Timestamp lastUsed;

    /**
     * 使用干什么
     */
    @Column(hump = true)
    String action;
}
