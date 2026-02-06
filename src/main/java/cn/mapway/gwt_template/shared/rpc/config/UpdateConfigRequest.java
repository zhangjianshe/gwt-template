package cn.mapway.gwt_template.shared.rpc.config;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.rpc.app.AppData;
import cn.mapway.gwt_template.shared.rpc.user.ldap.LdapSettings;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateConfigRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateConfigRequest")
public class UpdateConfigRequest implements Serializable, IsSerializable {
    AppData appData;
    LdapSettings ldapSettings;
}
