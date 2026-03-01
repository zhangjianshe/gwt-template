package cn.mapway.gwt_template.shared.rpc.ldap;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateLdapEntryRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateLdapEntryRequest")
public class UpdateLdapEntryRequest implements Serializable, IsSerializable {
    LdapNodeData nodeData;
}
