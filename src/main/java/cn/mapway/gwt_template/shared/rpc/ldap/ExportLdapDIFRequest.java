package cn.mapway.gwt_template.shared.rpc.ldap;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * ExportLdapDIFRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("ExportLdapDIFRequest")
public class ExportLdapDIFRequest implements Serializable, IsSerializable {
    String dn;
}
