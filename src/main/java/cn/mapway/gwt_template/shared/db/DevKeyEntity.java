package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

@Doc("密钥对")
@Table(DevKeyEntity.TABLE_NAME)
@Getter
@Setter
public class DevKeyEntity implements Serializable, IsSerializable {
    public static final String TABLE_NAME = "dev_key";
    public static final String FLD_NAME = "name";
    public static final String FLD_ID = "id";
    public static final String FLD_CREATE_TIME = "create_time";
    public static final String FLD_PRIVATE_KEY = "private_key";
    @Name
    @ColDefine(width = 64, notNull = true)
    @Comment("节点ID")
    String id;

    @ColDefine(width = 64, notNull = true)
    @Comment("密钥名称")
    @Column()
    String name;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 2048, notNull = true)
    @Comment("私钥")
    String privateKey;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 2048, notNull = true)
    @Comment("公钥")
    String publicKey;

    @Comment("创建时间")
    @ColDefine(type = ColType.DATETIME, width = 0)
    @Column(hump = true)
    Timestamp createTime;

    @Comment("用户ID")
    @Column(hump = true)
    Long userId;

}
