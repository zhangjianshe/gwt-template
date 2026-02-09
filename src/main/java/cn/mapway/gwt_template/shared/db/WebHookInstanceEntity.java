package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

@Doc("WebHook激活记录")
@Table(WebHookInstanceEntity.TABLE_NAME)
@Getter
@Setter
public class WebHookInstanceEntity implements Serializable, IsSerializable {
    public static final String TABLE_NAME = "web_hook_instance";
    public static final String FLD_ID = "id";
    public static final String FLD_WEBHOOK_ID = "webhook_id";
    public static final String FLD_CREATE_TIME = "create_time";

    @Name
    @ColDefine(width = 64, notNull = true)
    @Comment("项目ID")
    String id;

    @Comment("WBE HOOK ID")
    @Column(hump = true)
    @ColDefine(type = ColType.VARCHAR, width = 64)
    String webhookId;

    @Comment("创建时间")
    @ColDefine(type = ColType.DATETIME, width = 0)
    @Column(hump = true)
    Timestamp createTime;

    @Comment("用时")
    @Column(hump = true)
    @Default("0")
    Long duration;

    @Comment("是否成功")
    @Column(hump = true)
    @Default("false")
    Boolean success;

    @Comment("Return Code")
    @Column(hump = true)
    Integer responseCode;

    @Comment("请求实体")
    @Column(hump = true)
    @ColDefine(type = ColType.TEXT)
    String requestBody;

    @Comment("返回实体")
    @Column(hump = true)
    @ColDefine(type = ColType.TEXT)
    String responseBody;
}
