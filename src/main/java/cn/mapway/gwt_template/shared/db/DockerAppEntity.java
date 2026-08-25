package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;

@Doc("Docker App Entity")
@Table(DockerAppEntity.TABLE_NAME)
@Getter
@Setter
public class DockerAppEntity implements Serializable, IsSerializable {
    public static final String TABLE_NAME = "docker_app";
    @Name
    @ColDefine(notNull = true)
    @Comment("app id")
    String id;

    @ColDefine(type = ColType.VARCHAR, width = 512, notNull = true)
    @Comment("app name")
    String name;

    @ColDefine(type = ColType.VARCHAR, width = 1024)
    @Comment("summary")
    String summary;

    @ColDefine(type = ColType.VARCHAR, width = 1024)
    @Comment("绝对路径")
    @Default("")
    String absolutePath;

}
