package cn.mapway.gwt_template.client.preference.app;

import cn.mapway.gwt_template.shared.rpc.config.ConfigEnums;
import cn.mapway.gwt_template.shared.rpc.config.QueryConfigResponse;
import cn.mapway.ui.client.mvc.attribute.AbstractAttributesProvider;
import cn.mapway.ui.client.mvc.attribute.DataCastor;
import cn.mapway.ui.client.mvc.attribute.IAttribute;
import cn.mapway.ui.client.mvc.attribute.atts.ImageUploadBoxAttribute;
import cn.mapway.ui.client.mvc.attribute.atts.TextBoxAttribute;
import cn.mapway.ui.client.mvc.attribute.editor.ParameterKeys;
import com.google.gwt.core.client.GWT;

import java.util.List;

public class ConfigAttrProvider extends AbstractAttributesProvider {
    QueryConfigResponse configResponse;

    public void rebuild(QueryConfigResponse configData) {
        this.configResponse = configData;
        List<IAttribute> attributes = getAttributes();
        attributes.clear();

        attributes.add(new ImageUploadBoxAttribute("logo", "应用LOGO") {
            @Override
            public Object getValue() {
                return configResponse.getAppData().getLogo();
            }

            @Override
            public String getGroup() {
                return ConfigEnums.CONFIG_APP.getValue();
            }

            @Override
            public void setValue(Object value) {
                configResponse.getAppData().setLogo(DataCastor.castToString(value));
            }
        }.param(ParameterKeys.KEY_HEIGHT, "150px")
                .param(ParameterKeys.KEY_IMAGE_UPLOAD_ACTION, GWT.getHostPageBaseURL() + "fileUpload")
                .param(ParameterKeys.KEY_IMAGE_UPLOAD_REL, "app"));


        attributes.add(new TextBoxAttribute("gitSSH", "GIT克隆模板") {
            @Override
            public Object getValue() {
                return configResponse.getAppData().getSshServer();
            }

            @Override
            public String getGroup() {
                return ConfigEnums.CONFIG_APP.getValue();
            }

            @Override
            public String getDescription() {
                String msg = "SSH 模板 git@dev.cangling.cn:/ownerName/projectName.git\n" +
                        "或者 ssh://userName@dev.cangling.cn:2222/ownerName/projectName.git<br/>";
                msg += "userName,ownerName,projectName是模板变量,如果端口是22请用第一模板";
                return msg;
            }

            @Override
            public void setValue(Object value) {
                configResponse.getAppData().setSshServer(DataCastor.castToString(value));
            }
        });

        attributes.add(new TextBoxAttribute("url", "LDAP URL") {
            @Override
            public Object getValue() {
                return configResponse.getLdapSettings().getUrl();
            }

            @Override
            public String getGroup() {
                return ConfigEnums.CONFIG_LDAP.getValue();
            }

            @Override
            public void setValue(Object value) {
                configResponse.getLdapSettings().setUrl(DataCastor.castToString(value));
            }
        });
        attributes.add(new TextBoxAttribute("baseDn", "基础DN") {
            @Override
            public Object getValue() {
                return configResponse.getLdapSettings().getBaseDn();
            }

            @Override
            public String getGroup() {
                return ConfigEnums.CONFIG_LDAP.getValue();
            }

            @Override
            public void setValue(Object value) {
                configResponse.getLdapSettings().setBaseDn(DataCastor.castToString(value));
            }
        });

        attributes.add(new TextBoxAttribute("searchPattern", "过滤用户规则") {
            @Override
            public Object getValue() {
                return configResponse.getLdapSettings().getSearchPattern();
            }

            @Override
            public String getGroup() {
                return ConfigEnums.CONFIG_LDAP.getValue();
            }

            @Override
            public void setValue(Object value) {
                configResponse.getLdapSettings().setSearchPattern(DataCastor.castToString(value));
            }
        });

        attributes.add(new TextBoxAttribute("manageDn", "管理 DN") {
            @Override
            public Object getValue() {
                return configResponse.getLdapSettings().getManagerDn();
            }

            @Override
            public String getGroup() {
                return ConfigEnums.CONFIG_LDAP.getValue();
            }

            @Override
            public void setValue(Object value) {
                configResponse.getLdapSettings().setManagerDn(DataCastor.castToString(value));
            }
        });

        attributes.add(new TextBoxAttribute("managPWD", "管理密码") {
            @Override
            public Object getValue() {
                return configResponse.getLdapSettings().getManagerPassword();
            }

            @Override
            public String getGroup() {
                return ConfigEnums.CONFIG_LDAP.getValue();
            }

            @Override
            public void setValue(Object value) {
                configResponse.getLdapSettings().setManagerPassword(DataCastor.castToString(value));
            }
        });


        notifyAttributeReady();
    }

    @Override
    public String getAttributeTitle() {
        return "应用信息";
    }
}
