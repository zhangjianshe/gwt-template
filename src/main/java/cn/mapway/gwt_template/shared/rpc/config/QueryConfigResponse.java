package cn.mapway.gwt_template.shared.rpc.config;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.rpc.app.AppData;
import cn.mapway.gwt_template.shared.rpc.user.ldap.LdapSettings;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryConfigResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryConfigResponse")
public class QueryConfigResponse implements Serializable, IsSerializable {
    LdapSettings ldapSettings;
    AppData  appData;
}
