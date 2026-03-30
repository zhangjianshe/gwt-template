package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;

/**
 * 项目部的Wiki页面
 */
@Doc("项目部的Wiki页面Block")
@Table(DevProjectPageSectionEntity.TBL_NAME)
@Comment("项目部的Wiki页面Block")
@Getter
@Setter
@TableIndexes({
        // 联合索引：快速定位某页面下的某个特定块的所有历史
        @Index(name = "idx_block_lookup", fields = {"pageId", "sectionId"}, unique = false)
})
public class DevProjectPageSectionEntity implements Serializable, IsSerializable {

    /* 表名常量 */
    public static final String TBL_NAME = "dev_project_page_section";

    /* 字段名常量 */
    public static final String FLD_VERSION = "version_id";
    public static final String FLD_PAGE_ID = "page_id";
    public static final String FLD_SECTION_ID = "section_id";
    public static final String FLD_KIND = "kind";
    public static final String FLD_CONTENT = "content";
    public static final String FLD_DATA = "data";


    @Column(FLD_VERSION)
    @Comment("version_id")
    @ColDefine(width = 64, notNull = true)
    @Name
    String versionId;


    @Column(FLD_PAGE_ID)
    @Comment("页面ID")
    @ColDefine(width = 64, notNull = true)
    String pageId;

    @Column(FLD_SECTION_ID)
    @Comment("SECTION")
    @ColDefine(width = 64, notNull = true)
    String sectionId;

    @Column(FLD_KIND)
    @Comment("Kind")
    Integer kind;


    @Column(FLD_CONTENT)
    @Comment("提交CONTENT")
    @ColDefine(type = ColType.TEXT)
    String content;

    @Column(FLD_DATA)
    @Comment("提交BLOB")
    @ColDefine(type = ColType.BINARY)
    byte[] data;
}