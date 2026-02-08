package cn.mapway.gwt_template.shared.db;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.View;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 我的项目视图
 */
@View("vw_project")
@Getter
@Setter
public class VwProjectEntity implements Serializable, IsSerializable {
    @Column("id")
    String id;  //项目ID
    @Column("name")
    String name;
    @Column("create_time")
    Timestamp createTime;
    @Column("user_id")
    Long userId; //创建者ID

    @Column(hump = true)
    String groupId;

    @Column("source_url")
    String sourceUrl;
    @Column("build_script")
    String buildScript;
    @Column("deploy_server")
    String deployServer;
    @Column("service_url")
    String serviceUrl;

    @Column("owner_kind")
    Integer ownerKind;
    @Column("permission")
    Integer permission;

    @Column("my_id")
    Long myId;//授权的用户ID

    @Column("summary")
    String summary;

    @Column(hump = true)
    Boolean isPublic;

    @Column(hump = true)
    Integer memberCount;

    @Column(hump = true)
    String ownerName;
}
