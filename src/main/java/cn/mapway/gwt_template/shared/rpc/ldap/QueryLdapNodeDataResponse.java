package cn.mapway.gwt_template.shared.rpc.ldap;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryLdapNodeDataResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryLdapNodeDataResponse")
public class QueryLdapNodeDataResponse implements Serializable, IsSerializable {
    List<LdapNodeData> nodes;
}
