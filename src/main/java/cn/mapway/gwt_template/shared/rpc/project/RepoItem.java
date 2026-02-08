package cn.mapway.gwt_template.shared.rpc.project;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class RepoItem implements Serializable, IsSerializable {
    private String name;
    private String path;
    private String summary;    // The commit message
    private Date date;         // Commit time
    private String author;     // Who made the change
    private boolean isDir;     // True if folder, False if file
    private Long size;     // True if folder, False if file
    private String hash;
}
