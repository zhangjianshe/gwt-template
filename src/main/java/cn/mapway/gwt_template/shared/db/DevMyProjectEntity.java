package cn.mapway.gwt_template.shared.db;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.PK;
import org.nutz.dao.entity.annotation.Table;

import java.io.Serializable;

@Table(DevMyProjectEntity.TABLE_NAME)
@PK(value = {"myId","projectId"})
public class DevMyProjectEntity implements Serializable, IsSerializable {
    public static final String TABLE_NAME = "dev_my_project";
    public static final String FLD_MYID = "my_id";
    public static final String FLD_PROJECT_ID = "project_id";

    @Column("my_id")
    public Long myId;
    @Column("project_id")
    public String projectId;
    @Column("permission")
    public Integer permission;
}
