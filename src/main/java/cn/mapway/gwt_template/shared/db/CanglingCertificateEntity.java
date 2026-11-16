package cn.mapway.gwt_template.shared.db;
import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;
import java.io.Serializable;
import java.sql.Timestamp;
@Doc("用户同步证书")
@Table("cangling_certificate")
@TableIndexes({@Index(name="idx_certificate_user_key", fields={"userId", "publicKey"}, unique=true)})
@Getter @Setter
public class CanglingCertificateEntity implements Serializable, IsSerializable {
    @Name @ColDefine(width=128) private String id;
    @Column("user_id") @ColDefine(notNull=true) private Long userId;
    @Column @ColDefine(width=256, notNull=true) private String name;
    @Column("public_key") @ColDefine(width=512, notNull=true) private String publicKey;
    @Column("private_key") @ColDefine(type=ColType.TEXT, notNull=true) private String privateKey;
    @Column("create_time") private Timestamp createTime;
    @Column("update_time") private Timestamp updateTime;
}
