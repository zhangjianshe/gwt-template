package cn.mapway.gwt_template.server.service;

import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.client.ldap.AttributeKind;
import cn.mapway.gwt_template.client.ldap.LdapNodeAttribute;
import cn.mapway.gwt_template.server.service.config.SystemConfigService;
import cn.mapway.gwt_template.shared.rpc.config.ConfigEnums;
import cn.mapway.gwt_template.shared.rpc.ldap.LdapNodeData;
import cn.mapway.gwt_template.shared.rpc.ldap.RootDse;
import cn.mapway.gwt_template.shared.rpc.user.ldap.LdapSettings;
import cn.mapway.ui.shared.IReset;
import lombok.extern.slf4j.Slf4j;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.repo.Base64;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class LdapService implements IReset {
    private static final Set<String> OPERATIONAL_ATTRS = Set.of(
            "createtimestamp", "modifytimestamp", "creatorsname",
            "modifiersname", "entrydn", "entrycsn", "entryuuid", "hassubordinates", "subschemasubentry",
            "structuralobjectclass"
    );
    @Resource
    SystemConfigService systemConfigService;
    LdapTemplate ldapTemplate;

    private LdapContextSource getContextSource() {

        LdapSettings settings = systemConfigService.getConfigFromKeyAsObject(ConfigEnums.CONFIG_LDAP.getCode(), LdapSettings.class);
        if (settings == null || Strings.isBlank(settings.getUrl())) {
            log.warn("[LDAP] 没有LDAP配置信息");
        }

        LdapContextSource cs = new LdapContextSource();
        cs.setUrl(settings.getUrl());
        cs.setBase("");
        cs.setUserDn(settings.getManagerDn());
        cs.setPassword(settings.getManagerPassword());

        // --- 新增超时配置 ---
        java.util.Map<String, Object> env = new java.util.HashMap<>();
        // 设置连接超时时间（毫秒），例如 5000ms = 5秒
        env.put("com.sun.jndi.ldap.connect.timeout", "5000");
        // 设置读取超时时间（毫秒）
        env.put("com.sun.jndi.ldap.read.timeout", "5000");
        cs.setBaseEnvironmentProperties(env);
        // ------------------
        cs.afterPropertiesSet();
        return cs;
    }

    public synchronized LdapTemplate getLdapTemplate() {
        if (ldapTemplate == null) {
            ldapTemplate = new LdapTemplate(getContextSource());
        }
        return ldapTemplate;
    }

    public RootDse getRootDse() {
        return getLdapTemplate().executeReadOnly(ctx -> {
            // Search the empty DN ("") with OBJECT scope
            // We specifically ask for the operational attributes
            Attributes attrs = ctx.getAttributes("", new String[]{
                    "namingContexts", "supportedLDAPVersion", "vendorName", "subschemaSubentry"
            });

            RootDse dse = new RootDse();
            dse.setNamingContexts(getAttributeValues(attrs.get("namingContexts")));
            dse.setSupportedLDAPVersion(getAttributeValues(attrs.get("supportedLDAPVersion")));

            if (attrs.get("vendorName") != null) {
                dse.setVendorName(attrs.get("vendorName").get().toString());
            }
            if (attrs.get("subschemaSubentry") != null) {
                dse.setSubschemaSubentry(attrs.get("subschemaSubentry").get().toString());
            }

            return dse;
        });
    }

    // Helper to convert LDAP Attribute to List<String>
    private List<String> getAttributeValues(Attribute attr) throws javax.naming.NamingException {
        List<String> values = new ArrayList<>();
        if (attr != null) {
            for (int i = 0; i < attr.size(); i++) {
                values.add(attr.get(i).toString());
            }
        }
        return values;
    }

    public List<LdapNodeData> getChildren(String parentDn) {
        String[] attrsToFetch = new String[]{"*", "structuralObjectClass", "objectClass"};
        return getLdapTemplate().search(
                LdapQueryBuilder.query()
                        .base(parentDn)
                        .searchScope(SearchScope.ONELEVEL)
                        .attributes(attrsToFetch)
                        .where("objectClass").isPresent(),
                (ContextMapper<LdapNodeData>) ctx -> {
                    DirContextAdapter adapter = (DirContextAdapter) ctx;
                    LdapNodeData node = new LdapNodeData();

                    node.setDn(adapter.getNameInNamespace());
                    node.setName(adapter.getStringAttribute("ou") != null ?
                            adapter.getStringAttribute("ou") :
                            adapter.getStringAttribute("cn"));
                    // Add logic here to determine if it's a "folder" or "leaf"
                    node.setFolder(false);

                    String[] classes = adapter.getStringAttributes("objectClass");
                    if (classes != null) {
                        node.setObjectClasses(Lang.array2list(classes));
                        node.setFolder(node.getObjectClasses().contains("organizationalUnit"));
                    }
                    String structuralObjectClass = adapter.getStringAttribute("structuralObjectClass");
                    node.setStructuralObjectClass(structuralObjectClass);

                    return node;
                }
        );
    }

    public LdapNodeData getEntryDetails(String dn) {
        return getLdapTemplate().lookup(dn, new String[]{"*", "+"}, (Attributes attrs) -> {
            LdapNodeData details = new LdapNodeData();
            details.setDn(dn);

            NamingEnumeration<String> ids = attrs.getIDs();
            while (ids.hasMore()) {
                String id = ids.next();
                Attribute attr = attrs.get(id);
                if (id.equals("ou")) {
                    details.setName(attr.get().toString());
                    continue;
                } else if (id.equals("cn")) {
                    details.setName(attr.get().toString());
                    continue;
                } else if (id.equalsIgnoreCase("objectClass")) {
                    details.setFolder(false);
                    List<String> classes = new ArrayList<>();
                    for (int i = 0; i < attr.size(); i++) {
                        String cls = (String) attr.get(i);
                        classes.add(cls);
                        if ("organizationalUnit".equalsIgnoreCase(cls)) {
                            details.setFolder(true);
                        }
                    }
                    details.setObjectClasses(classes);
                    continue;
                } else if (id.toLowerCase().contains("password")) {
                    LdapNodeAttribute attribute = new LdapNodeAttribute();
                    attribute.setKey(id);
                    attribute.setValue("********");
                    attribute.setKind(AttributeKind.AK_PASSWORD.getKind()); // If you have this enum
                    attribute.setSysData(false);
                    details.getAttributes().add(attribute);
                    continue;
                } else if (id.equalsIgnoreCase("structuralObjectClass")) {
                    details.setStructuralObjectClass(attr.get().toString());
                    continue;
                }

                for (int i = 0; i < attr.size(); i++) {
                    Object val = attr.get(i);
                    LdapNodeAttribute detail = new LdapNodeAttribute();
                    detail.setKey(id);
                    detail.setSysData(OPERATIONAL_ATTRS.contains(id.toLowerCase()));
                    if (val instanceof byte[]) {
                        detail.setValue(Base64.encodeToString((byte[]) val, false));
                        detail.setKind(AttributeKind.AK_BLOB.getKind());
                    } else if (val instanceof String) {
                        detail.setValue((String) val);
                        detail.setKind(AttributeKind.AK_STRING.getKind());
                    } else if (val instanceof Number) {
                        detail.setValue(String.valueOf(val));
                        detail.setKind(AttributeKind.AK_NUMBER.getKind());
                    } else {
                        detail.setValue(String.valueOf(val));
                        detail.setKind(AttributeKind.AK_UNKNOWN.getKind());
                    }
                    details.getAttributes().add(detail);
                }
            }
            return details;
        });
    }

    @Override
    public void reset() {
        ldapTemplate = null;
    }

    /**
     * 更新一个DN
     *
     * @param nodeData
     * @return
     */
    public BizResult<LdapNodeData> updateLdapEntry(LdapNodeData nodeData) {
        try {
            List<ModificationItem> modificationItems = new ArrayList<>();
            String currentDn = nodeData.getDn();
            for (LdapNodeAttribute attr : nodeData.getAttributes()) {
                // 1. Skip operational/system data - LDAP doesn't allow direct modification of these
                if (OPERATIONAL_ATTRS.contains(attr.getKey().toLowerCase())) {
                    continue;
                }

                // 2. Special handling for Passwords
                if (AttributeKind.AK_PASSWORD.getKind().equals(attr.getKind())) {
                    // Only update if the user actually typed a new password (not the mask)
                    if (!"********".equals(attr.getValue()) && Strings.isNotBlank(attr.getValue())) {
                        LdapShaPasswordEncoder encoder = new LdapShaPasswordEncoder();
                        String hashedRow = encoder.encode(attr.getValue());
                        modificationItems.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                                new BasicAttribute(attr.getKey(), hashedRow)));
                    }
                    continue;
                }

                // 3. Handle Binary data vs String data
                if (AttributeKind.AK_BLOB.getKind().equals(attr.getKind())) {
                    byte[] bytes = Base64.decode(attr.getValue());
                    modificationItems.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                            new BasicAttribute(attr.getKey(), bytes)));
                } else {
                    // Standard String modification
                    modificationItems.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                            new BasicAttribute(attr.getKey(), attr.getValue())));
                }

            }

            if (!modificationItems.isEmpty()) {
                getLdapTemplate().modifyAttributes(nodeData.getDn(),
                        modificationItems.toArray(new ModificationItem[0]));
                log.info("[LDAP] Success updating DN: {}", nodeData.getDn());
            }

            // 2. Handle Rename (RDN Change)
            // We need to determine if the name (the part after cn= or ou=) changed.
            LdapNodeData existing = getEntryDetails(currentDn);
            if (Strings.isNotBlank(nodeData.getName()) && !nodeData.getName().equals(existing.getName())) {

                // Build the new DN based on whether it's an OU or a Leaf (CN/UID)
                String prefix = nodeData.isFolder() ? "ou=" : "cn=";

                // Extract the parent DN (e.g., everything after the first comma)
                int firstComma = currentDn.indexOf(",");
                if (firstComma > 0) {
                    String parentDn = currentDn.substring(firstComma + 1);
                    String newDn = prefix + nodeData.getName() + "," + parentDn;

                    log.info("[LDAP] Renaming {} to {}", currentDn, newDn);
                    getLdapTemplate().rename(currentDn, newDn);

                    // Update the local reference so getEntryDetails works below
                    currentDn = newDn;
                }
            }

            // Return the fresh data after update
            return BizResult.success(getEntryDetails(nodeData.getDn()));

        } catch (Exception e) {
            log.error("[LDAP] Error updating entry: " + nodeData.getDn(), e);
            return BizResult.error(500, e.getMessage());
        }
    }

    public BizResult<LdapNodeData> addLdapEntry(String parentDn, LdapNodeData newNode) {
        try {
            // 1. Construct the new Full DN
            // Example: cn=newuser,ou=users,dc=cangling,dc=cn
            String rdnPrefix = newNode.isFolder() ? "ou=" : "cn=";
            String newDn = rdnPrefix + newNode.getName() + "," + parentDn;

            // 2. Create the Adapter
            DirContextAdapter adapter = new DirContextAdapter(newDn);

            // 3. Set the Structural and Auxiliary Object Classes
            // Every entry MUST have objectClass values to be valid
            for (String oc : newNode.getObjectClasses()) {
                adapter.addAttributeValue("objectClass", oc);
            }


            // 4. Set the Attributes from your DTO
            for (LdapNodeAttribute attr : newNode.getAttributes()) {
                if (Strings.isBlank(attr.getValue())) continue;

                if (AttributeKind.AK_PASSWORD.getKind().equals(attr.getKind())) {
                    // Only update if the user actually typed a new password (not the mask)
                    if (!"********".equals(attr.getValue()) && Strings.isNotBlank(attr.getValue())) {
                        LdapShaPasswordEncoder encoder = new LdapShaPasswordEncoder();
                        String hashedRow = encoder.encode(attr.getValue());
                        adapter.setAttributeValue(attr.getKey(), hashedRow);
                    }
                    continue;
                }

                if (AttributeKind.AK_BLOB.getKind().equals(attr.getKind())) {
                    adapter.setAttributeValue(attr.getKey(), Base64.decode(attr.getValue()));
                } else {
                    adapter.setAttributeValue(attr.getKey(), attr.getValue());
                }
            }

            // 5. Bind to the LDAP Server
            getLdapTemplate().bind(adapter);

            log.info("[LDAP] Created new entry: {}", newDn);
            return BizResult.success(getEntryDetails(newDn));

        } catch (Exception e) {
            log.error("[LDAP] Error adding entry to " + parentDn, e);
            return BizResult.error(500, "Create failed: " + e.getMessage());
        }
    }
}
