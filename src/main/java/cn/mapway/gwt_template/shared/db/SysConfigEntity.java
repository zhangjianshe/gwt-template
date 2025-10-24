package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.sql.Timestamp;

@Doc("系统配置表")
@Table(SysConfigEntity.TABLE_NAME)
@Getter
@Setter
public class SysConfigEntity {
    public static final String TABLE_NAME = "sys_config";
    @Name
    @ColDefine(width = 128, notNull = true)
    @Comment("配置键")
    String key;

    @Column
    @ColDefine(type = ColType.TEXT, notNull = true)
    @Default(value = "")
    @Comment("配置值")
    String value;

    @Comment("创建时间")
    @ColDefine(type = ColType.DATETIME, width = 0)
    @Column(hump = true)
    Timestamp createTime;
}
