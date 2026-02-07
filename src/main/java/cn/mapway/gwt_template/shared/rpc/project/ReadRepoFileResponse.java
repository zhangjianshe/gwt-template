package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * ReadRepoFileResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("ReadRepoFileResponse")
public class ReadRepoFileResponse implements Serializable, IsSerializable {
    String text;
}
