package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

@Doc("系统软件关联的文件")
@Table(SysSoftwareFileEntity.TABLE_NAME)
@Getter
@Setter
public class SysSoftwareFileEntity implements Serializable, IsSerializable {
    public static final String TABLE_NAME = "sys_software_file";
    public static final String FLD_ID = "id";
    public static final String FLD_SOFTWARE_ID = "software_id";
    public static final String FLD_VERSION = "version";
    public static final String FLD_NAME = "name";
    public static final String FLD_SUMMARY = "summary";
    public static final String FLD_CREATE_TIME = "create_time";
    @Name
    @ColDefine(width = 128, notNull = true)
    @Comment("ID")
    String id;

    @Column("software_id")
    @ColDefine(type = ColType.VARCHAR, width = 64, notNull = true)
    @Comment("软件ID")
    String softwareId;


    @Column
    @ColDefine(type = ColType.VARCHAR, width = 64, notNull = true)
    @Comment("软件版本")
    String version;

    @Column("name")
    @ColDefine(type = ColType.VARCHAR, width = 1024, notNull = true)
    @Comment("fileName")
    String name;

    @Column
    @Comment("file size")
    @Default("0")
    Long size;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 512)
    @Comment("文件所在位置")
    String location;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 64)
    @Comment("操作系统")
    String os;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 64)
    @Comment("CPU架构")
    String arch;

    @Column
    @ColDefine(type = ColType.VARCHAR, width = 1024)
    @Comment("SUMMARY")
    String summary;

    @Column("create_time")
    @Comment("create_time")
    Timestamp createTime;
}
