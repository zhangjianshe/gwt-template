package cn.mapway.gwt_template.shared.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;
import java.sql.Timestamp;

@Table("drive_token")
@TableIndexes({@Index(name="idx_drive_token_hash", fields={"tokenHash"}, unique=true),
        @Index(name="idx_drive_token_directory", fields={"directoryId"}, unique=false)})
@Getter @Setter
public class DriveTokenEntity {
    @Name @ColDefine(width=32) private String id;
    @Column("directory_id") @ColDefine(width=32, notNull=true) private String directoryId;
    @Column @ColDefine(width=100, notNull=true) private String name;
    @JsonIgnore @Column("token_hash") @ColDefine(width=64, notNull=true) private String tokenHash;
    @Column @ColDefine(notNull=true) private Integer permissions;
    @Column("create_time") private Timestamp createTime;
}
