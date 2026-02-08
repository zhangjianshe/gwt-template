package cn.mapway.gwt_template.shared.rpc.project.git;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class GitRef implements Serializable, IsSerializable {
    /**
     * 0 BRANCH
     * 1 TAG
     */
    Integer kind;
    String name;

    public static GitRef branch(String branch) {
        GitRef ref = new GitRef();
        ref.kind = 0;
        ref.name = branch;
        return ref;
    }

    public static GitRef tag(String tag) {
        GitRef ref = new GitRef();
        ref.kind = 1;
        ref.name = tag;
        return ref;
    }
}
