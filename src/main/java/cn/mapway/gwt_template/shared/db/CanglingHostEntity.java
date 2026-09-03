package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 主机配置（维护中心同步使用）。
 * <p>
 * 私有主机只属于创建者；{@code isPublic=1} 的主机对所有拥有
 * {@code ROLE_HOST_PUBLIC} 角色的用户可见。认证信息（密码 / 私钥）也会
 * 一并同步，因此只有受信任的用户才能获得公共主机访问角色。
 */
@Doc("主机配置")
@Table(CanglingHostEntity.TABLE_NAME)
@Getter
@Setter
public class CanglingHostEntity implements Serializable, IsSerializable {
    public static final String TABLE_NAME = "cangling_host";
    public static final String FLD_ID = "id";
    public static final String FLD_USER_ID = "user_id";
    public static final String FLD_NAME = "name";
    public static final String FLD_HOSTNAME = "hostname";
    public static final String FLD_PORT = "port";
    public static final String FLD_UPDATE_PORT = "update_port";
    public static final String FLD_USERNAME = "username";
    public static final String FLD_AUTH_METHOD = "auth_method";
    public static final String FLD_PASSWORD = "password";
    public static final String FLD_PRIVATE_KEY = "private_key";
    public static final String FLD_PUBLIC_KEY = "public_key";
    public static final String FLD_INJECT_REMOTE_PORT = "inject_remote_port";
    public static final String FLD_CATALOG = "catalog";
    public static final String FLD_IS_PUBLIC = "is_public";
    public static final String FLD_CREATE_TIME = "create_time";
    public static final String FLD_UPDATE_TIME = "update_time";

    @Name
    @ColDefine(width = 128, notNull = true)
    @Comment("ID")
    private String id;

    @Column("user_id")
    @Comment("所属用户ID")
    private Long userId;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 256, notNull = true)
    @Comment("名称")
    private String name;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 256, notNull = true)
    @Comment("主机名/IP")
    private String hostname;

    @Column
    @ColDefine(type = ColType.INT, notNull = true)
    @Comment("SSH端口")
    private Integer port;

    @Column("update_port")
    @ColDefine(type = ColType.INT, notNull = true)
    @Default("5400")
    @Comment("cangling-update 服务端口")
    private Integer updatePort;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 256, notNull = true)
    @Comment("用户名")
    private String username;

    @Column("auth_method")
    @ColDefine(type = ColType.VARCHAR, width = 32, notNull = true)
    @Comment("认证方式 password|certificate")
    private String authMethod;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 2048)
    @Comment("密码")
    private String password;

    @Column("private_key")
    @ColDefine(type = ColType.TEXT)
    @Comment("私钥内容")
    private String privateKey;

    @Column("public_key")
    @ColDefine(type = ColType.TEXT)
    @Comment("公钥内容")
    private String publicKey;

    @Column("inject_remote_port")
    @ColDefine(type = ColType.INT, notNull = true)
    @Default("7890")
    @Comment("远端代理注入端口")
    private Integer injectRemotePort;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 128)
    @Comment("分组/目录，例如 河南")
    private String catalog;

    @Column("is_public")
    @ColDefine(type = ColType.INT, notNull = true)
    @Default("0")
    @Comment("是否公共主机 0私有 1公共")
    private Integer isPublic;

    @Column("create_time")
    @Comment("创建时间")
    private Timestamp createTime;

    @Column("update_time")
    @Comment("更新时间")
    private Timestamp updateTime;

    /**
     * 是否属于当前登录用户。仅列表/保存接口计算返回，不是数据库字段。
     */
    @Readonly
    private Boolean mine;
}
