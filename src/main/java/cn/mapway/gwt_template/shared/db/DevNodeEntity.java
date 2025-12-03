package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

@Doc("服务器节点")
@Table(DevNodeEntity.TABLE_NAME)
@Getter
@Setter
public class DevNodeEntity implements Serializable, IsSerializable {
    public static final String TABLE_NAME = "dev_node";
    public static final String FLD_ID = "id";
    @Name
    @ColDefine(width = 64, notNull = true)
    @Comment("节点ID")
    String id;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 512, notNull = true)
    @Comment("节点名称")
    String name;

    @Comment("创建时间")
    @ColDefine(type = ColType.DATETIME, width = 0)
    @Column(hump = true)
    Timestamp createTime;

    @Comment("节点IP")
    @Column(hump = true)
    String ip;

    @Comment("节点PORT")
    @Column(hump = true)
    String port;

    @Comment("ssh user")
    @Column(hump = true)
    String  sshUser;

    @Comment("key id")
    @Column(hump = true)
    String  keyId;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 512)
    @Comment("节点说明")
    String summary;

}
