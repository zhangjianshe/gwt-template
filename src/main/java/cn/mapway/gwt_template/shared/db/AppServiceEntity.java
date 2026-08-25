package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

@Doc("系统服务项")
@Table(AppServiceEntity.TABLE_NAME)
@Getter
@Setter
public class AppServiceEntity implements Serializable, IsSerializable {
    public static final String TABLE_NAME = "app_service";
    public static final String FLD_ID = "id";
    public static final String FLD_USER_ID = "user_id";
    public static final String FLD_CREATE_TIME = "createTime";
    public static final String FLD_ACTIVE = "active";
    @Name
    @ColDefine(width = 64, notNull = true)
    @Comment("服务ID")
    String id;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 512, notNull = true)
    @Comment("服务名称")
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


    @Comment("是否启用")
    @Column(hump = true)
    @Default("false")
    Boolean active;


    @Comment("关联终端节点,;隔开")
    @Column(hump = true)
    @Default("")
    String endPoints;

    @Comment("TLS")
    @Column(hump = true)
    @Default("")
    String tls;

    @Comment("rules")
    @Column(hump = true)
    @ColDefine(type = ColType.VARCHAR, width = 1024, notNull = true)
    String rule;

    @Comment("rules")
    @Column(hump = true)
    @ColDefine(type = ColType.VARCHAR, width = 1024, notNull = true)
    String balancer;

    @Comment("domain,“,号隔开的域名列表")
    @Column(hump = true)
    @ColDefine(type = ColType.VARCHAR, width = 2046)
    @Default("")
    String domains;
}
