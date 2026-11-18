package cn.mapway.gwt_template.shared.db;

import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

/** One durable counter record per directory, including the virtual root. */
@Table("drive_directory_stats")
@Getter
@Setter
public class DriveDirectoryStatsEntity {
  @Name
  @ColDefine(width = 32)
  private String id;

  @Column("file_count")
  private Long fileCount;

  @Column("directory_count")
  private Long directoryCount;

  @Column("total_size")
  private Long totalSize;

  @Column private Boolean initialized;

  @Column
  @ColDefine(width = 16)
  private String state;

  @Column("updated_time")
  private Timestamp updatedTime;

  @Column("calibrated_time")
  private Timestamp calibratedTime;
}
