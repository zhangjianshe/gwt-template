package cn.mapway.gwt_template.shared.rpc.ldap;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * ExportLdapDIFResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("ExportLdapDIFResponse")
public class ExportLdapDIFResponse implements Serializable, IsSerializable {
    String ldif;
}
