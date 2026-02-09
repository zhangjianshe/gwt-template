package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

@Doc("WebHook记录")
@Table(WebHookEntity.TABLE_NAME)
@Getter
@Setter
public class WebHookEntity implements Serializable, IsSerializable {
    public static final String TABLE_NAME = "web_hook";
    public static final String FLD_ID = "id";
    public static final String FLD_SOURCE_KIND = "source_kind";
    public static final String FLD_TARGET_KIND = "target_kind";
    public static final String FLD_SOURCE_ID = "source_id";
    public static final String FLD_CREATE_TIME = "create_time";

    @Name
    @ColDefine(width = 64, notNull = true)
    @Comment("项目ID")
    String id;

    @Comment("创建时间")
    @ColDefine(type = ColType.DATETIME, width = 0)
    @Column(hump = true)
    Timestamp createTime;


    @Comment("关联事件源分类")
    @Column(hump = true)
    Integer sourceKind;

    @Comment("关联事件源ID")
    @Column(hump = true)
    @ColDefine(type = ColType.VARCHAR, width = 64)
    String sourceId;

    @Comment("关联事件源类型触发条件")
    @Column(hump = true)
    @ColDefine(type = ColType.VARCHAR, width = 512)
    String sourceEvent;

    @Comment("关联事件源过滤器")
    @Column(hump = true)
    @ColDefine(type = ColType.VARCHAR, width = 512)
    String sourceFilter;

    @Comment("是否激活")
    @Column(hump = true)
    @Default("false")
    Boolean active;

    @Comment("授权码")
    @Column(hump = true)
    @ColDefine(type = ColType.VARCHAR, width = 512)
    String authorizeKey;


    @Comment("访问头`,`号隔开")
    @Column(hump = true)
    @ColDefine(type = ColType.VARCHAR, width = 1024)
    String headers;

    @Comment("激活次数")
    @Column(hump = true)
    @Default("0")
    Integer activeCount;

    @Comment("目标网址")
    @Column(hump = true)
    Integer targetKind;

    @Comment("目标网址")
    @Column(hump = true)
    String targetUrl;

    @Comment("数据内容")
    @Column(hump = true)
    String contentType;

    @Comment("调用方法")
    @Column(hump = true)
    String method;
}
