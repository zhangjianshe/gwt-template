package cn.mapway.gwt_template.shared.db;

import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

@Table("drive_user_grant")
@TableIndexes(
    @Index(
        name = "idx_drive_grant_directory_user",
        fields = {"directoryId", "userId"},
        unique = true))
@Getter
@Setter
public class DriveUserGrantEntity {
  @Name
  @ColDefine(width = 32)
  private String id;

  @Column("directory_id")
  @ColDefine(width = 32, notNull = true)
  private String directoryId;

  @Column("user_id")
  @ColDefine(notNull = true)
  private Long userId;

  @Column
  @ColDefine(notNull = true)
  private Integer permissions;

  @Column("granted_by")
  private Long grantedBy;

  @Column("create_time")
  private Timestamp createTime;
}
