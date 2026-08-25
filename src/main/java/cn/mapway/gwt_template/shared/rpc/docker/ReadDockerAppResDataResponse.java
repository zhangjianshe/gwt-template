package cn.mapway.gwt_template.shared.rpc.docker;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.rpc.project.module.PreviewData;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * ReadDockerAppResDataResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("ReadDockerAppResDataResponse")
public class ReadDockerAppResDataResponse implements Serializable, IsSerializable {
    PreviewData previewData;
}
