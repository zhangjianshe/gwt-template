package cn.mapway.gwt_template.client.ldap;

import cn.mapway.gwt_template.shared.rpc.ldap.LdapNodeData;
import cn.mapway.ui.client.mvc.attribute.AbstractAttributesProvider;
import cn.mapway.ui.client.mvc.attribute.DataCastor;
import cn.mapway.ui.client.mvc.attribute.IAttribute;
import cn.mapway.ui.client.mvc.attribute.atts.LabelAttribute;
import cn.mapway.ui.client.mvc.attribute.atts.TextBoxAttribute;

import java.util.ArrayList;
import java.util.List;


public class LdapNodeAttrProvider extends AbstractAttributesProvider {
    LdapNodeData nodeData;

    List<String> readableAttributes;

    public LdapNodeAttrProvider() {
        readableAttributes = new ArrayList<>();
        readableAttributes.add("uid");
    }

    @Override
    public String getAttributeTitle() {
        if (nodeData == null) {
            return "选择目录";
        } else {
            return nodeData.getName();
        }
    }


    public void rebuild(LdapNodeData nodeData) {
        this.nodeData = nodeData;
        List<IAttribute> attributes = getAttributes();
        attributes.clear();
        if (nodeData == null) {
            notifyAttributeReady();
            return;
        }
        attributes.add(new LabelAttribute("DN", "全名称") {
            @Override
            public Object getValue() {
                return nodeData.getDn();
            }
        });

        attributes.add(new TextBoxAttribute("Name", "名称") {
            @Override
            public Object getValue() {
                return nodeData.getName();
            }

            @Override
            public void setValue(Object value) {
                nodeData.setName(DataCastor.castToString(value));
            }
        });

        attributes.add(new LabelAttribute("type", "主要类型") {
            @Override
            public Object getValue() {
                return nodeData.getStructuralObjectClass();
            }
        });
        attributes.add(new LabelAttribute("kind", "schema") {
            @Override
            public Object getValue() {
                StringBuilder list = new StringBuilder();
                if (nodeData.getObjectClasses() != null) {
                    for (String objectClass : nodeData.getObjectClasses()) {
                        if (list.length() > 0) {
                            list.append(",");
                        }
                        list.append(objectClass);
                    }
                }
                return list.toString();
            }
        });

        for (LdapNodeAttribute att : nodeData.getAttributes()) {
            if (att.sysData) {
                attributes.add(new LabelAttribute(att.getKey(), att.getKey()) {
                    @Override
                    public Object getValue() {
                        return att.getValue();
                    }

                    @Override
                    public String getGroup() {
                        return "系统数据";
                    }
                });
            } else if (readableAttributes.contains(att.getKey().toLowerCase())) {
                attributes.add(new LabelAttribute(att.getKey(), att.getKey()) {

                    @Override
                    public Object getValue() {
                        return att.getValue();
                    }
                });
            } else {
                attributes.add(new TextBoxAttribute(att.getKey(), att.getKey()) {

                    @Override
                    public Object getValue() {
                        return att.getValue();
                    }

                    @Override
                    public void setValue(Object value) {
                        att.setValue(DataCastor.castToString(value));
                    }
                });
            }
        }
        notifyAttributeReady();
    }
}
