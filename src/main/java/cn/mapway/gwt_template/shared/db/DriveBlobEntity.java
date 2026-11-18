package cn.mapway.gwt_template.shared.db;

import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

/** SHA-256 is the unique content identity; entries retain independent IDs and permissions. */
@Table("drive_blob")
@Getter
@Setter
public class DriveBlobEntity {
  @Name
  @ColDefine(width = 64)
  private String id;

  @Column
  @ColDefine(customType = "bigint", notNull = true)
  private Long size;

  @Column("create_time")
  private Timestamp createTime;
}
