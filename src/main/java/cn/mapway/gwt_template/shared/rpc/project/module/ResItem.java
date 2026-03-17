package cn.mapway.gwt_template.shared.rpc.project.module;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 用于表示磁盘上的一个文件或者目录
 */
@Getter
@Setter
public class ResItem implements Serializable, IsSerializable {
    String pathName;
    Double fileSize;
    Double lastModified;
    Boolean isDir;
}
