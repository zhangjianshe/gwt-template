package cn.mapway.gwt_template.shared.db;

import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

/** Logical paths are stored in the database; filenames never become disk paths. */
@Table("drive_entry")
@TableIndexes({
  @Index(
      name = "idx_drive_parent_name",
      fields = {"parentId", "name"},
      unique = true),
  @Index(
      name = "idx_drive_entry_blob",
      fields = {"blobId"},
      unique = false)
})
@Getter
@Setter
public class DriveEntryEntity {
  @Name
  @ColDefine(width = 32)
  private String id;

  @Column("parent_id")
  @ColDefine(width = 32, notNull = true)
  private String parentId;

  @Column
  @ColDefine(width = 200, notNull = true)
  private String name;

  @Column
  @ColDefine(notNull = true)
  private Boolean directory;

  @Column private Long size;

  @Column
  @ColDefine(width = 64)
  private String sha256;

  @Column("blob_id")
  @ColDefine(width = 64)
  private String blobId;

  @Column("create_time")
  private Timestamp createTime;
}
