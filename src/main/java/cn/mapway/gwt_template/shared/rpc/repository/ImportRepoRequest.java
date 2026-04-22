package cn.mapway.gwt_template.shared.rpc.repository;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * ImportRepoRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("ImportRepoRequest")
public class ImportRepoRequest implements Serializable, IsSerializable {
    String repositoryId;
    String repoUrl;
    String user;
    String tokenOrPassword;
    String newRepositoryName;
}
