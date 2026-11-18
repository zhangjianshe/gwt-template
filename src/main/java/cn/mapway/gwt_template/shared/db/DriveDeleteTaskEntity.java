package cn.mapway.gwt_template.shared.db;

import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

@Table("drive_delete_task")
@Getter
@Setter
public class DriveDeleteTaskEntity {
  @Name
  @ColDefine(width = 32)
  private String id;

  @Column("owner_id")
  private Long ownerId;

  @Column
  @ColDefine(width = 16)
  private String state;

  @Column private Boolean recursive;
  @Column private Integer total;
  @Column private Integer processed;
  @Column private Integer succeeded;
  @Column private Integer failed;
  @Column private Long version;

  @Column
  @ColDefine(width = 32)
  private String phase;

  @Column("current_name")
  @ColDefine(width = 200)
  private String currentName;

  @Column("entry_processed")
  private Long entryProcessed;

  @Column("entry_total")
  private Long entryTotal;

  @Column("items_json")
  @ColDefine(customType = "text")
  private String itemsJson;

  @Column("watch_hash")
  @ColDefine(width = 64)
  private String watchHash;

  @Column("created_time")
  private Timestamp createdTime;

  @Column("updated_time")
  private Timestamp updatedTime;
}
