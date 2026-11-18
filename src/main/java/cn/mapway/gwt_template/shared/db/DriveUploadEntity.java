package cn.mapway.gwt_template.shared.db;

import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

/** Durable acknowledgement state. Contains no credential or user supplied disk path. */
@Table("drive_upload")
@TableIndexes({
  @Index(
      name = "idx_drive_upload_updated",
      fields = {"updateTime"},
      unique = false)
})
@Getter
@Setter
public class DriveUploadEntity {
  @Name
  @ColDefine(width = 32)
  private String id;

  @Column("parent_id")
  @ColDefine(width = 32, notNull = true)
  private String parentId;

  @Column("entry_id")
  @ColDefine(width = 32, notNull = true)
  private String entryId;

  @Column
  @ColDefine(width = 200, notNull = true)
  private String name;

  @Column
  @ColDefine(width = 64, notNull = true)
  private String sha256;

  @Column
  @ColDefine(width = 64, notNull = true)
  private String owner;

  @Column("total_size")
  private Long totalSize;

  @Column("received_size")
  private Long receivedSize;

  @Column private Boolean completed;

  @Column("update_time")
  private Timestamp updateTime;
}
