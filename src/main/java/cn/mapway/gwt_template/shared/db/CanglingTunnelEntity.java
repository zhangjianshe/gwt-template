package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

/** 本地 SSH 隧道配置（按用户同步）。 */
@Doc("本地隧道配置")
@Table(CanglingTunnelEntity.TABLE_NAME)
@Getter
@Setter
public class CanglingTunnelEntity implements Serializable, IsSerializable {
    public static final String TABLE_NAME = "cangling_tunnel";
    public static final String FLD_ID = "id";
    public static final String FLD_USER_ID = "user_id";
    public static final String FLD_UPDATE_TIME = "update_time";

    @Name
    @ColDefine(width = 128, notNull = true)
    private String id;

    @Column("user_id")
    @Comment("所属用户ID")
    private Long userId;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 256, notNull = true)
    private String name;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 16, notNull = true)
    @Default("local")
    private String direction;

    @Column("local_host")
    @ColDefine(type = ColType.VARCHAR, width = 256, notNull = true)
    @Default("127.0.0.1")
    private String localHost;

    @Column("local_port")
    @ColDefine(type = ColType.INT, notNull = true)
    private Integer localPort;

    @Column("remote_host")
    @ColDefine(type = ColType.VARCHAR, width = 256, notNull = true)
    private String remoteHost;

    @Column("remote_port")
    @ColDefine(type = ColType.INT, notNull = true)
    private Integer remotePort;

    @Column("ssh_host")
    @ColDefine(type = ColType.VARCHAR, width = 256, notNull = true)
    private String sshHost;

    @Column("ssh_port")
    @ColDefine(type = ColType.INT, notNull = true)
    @Default("22")
    private Integer sshPort;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 256, notNull = true)
    private String username;

    @Column("auth_method")
    @ColDefine(type = ColType.VARCHAR, width = 32, notNull = true)
    @Default("password")
    private String authMethod;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 2048)
    private String password;

    @Column("private_key")
    @ColDefine(type = ColType.TEXT)
    private String privateKey;

    @Column("public_key")
    @ColDefine(type = ColType.TEXT)
    private String publicKey;

    @Column("create_time")
    private Timestamp createTime;

    @Column("update_time")
    private Timestamp updateTime;
}
