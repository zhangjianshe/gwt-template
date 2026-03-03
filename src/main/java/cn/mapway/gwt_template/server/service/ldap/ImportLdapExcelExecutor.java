package cn.mapway.gwt_template.server.service.ldap;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.client.ldap.AttributeKind;
import cn.mapway.gwt_template.client.ldap.LdapNodeAttribute;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.ldap.ImportLdapExcelRequest;
import cn.mapway.gwt_template.shared.rpc.ldap.ImportLdapExcelResponse;
import cn.mapway.gwt_template.shared.rpc.ldap.LdapNodeData;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.server.service.RbacUserService;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * ImportLdapExcelExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class ImportLdapExcelExecutor extends AbstractBizExecutor<ImportLdapExcelResponse, ImportLdapExcelRequest> {
    @Resource
    LdapService ldapService;
    @Resource
    private RbacUserService rbacUserService;

    @Override
    protected BizResult<ImportLdapExcelResponse> process(BizContext context, BizRequest<ImportLdapExcelRequest> bizParam) {
        ImportLdapExcelRequest request = bizParam.getData();
        log.info("ImportLdapExcelExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getDn()), "没有parentDn");
        assertTrue(Strings.isNotBlank(request.getData()), "没有要导入的数据");
        BizResult<Boolean> permission = rbacUserService.isAssignRole(user, "", AppConstant.ROLE_SYS_LDAP_MANAGER);
        assertTrue(permission.isSuccess() && permission.getData(), "没有操作LDAP权限");
        List<LdapNodeData> users = new ArrayList<>();
        Streams.eachLine(Lang.inr(request.getData()), new Each<String>() {
            @Override
            public void invoke(int index, String ele, int length) throws ExitLoop, ContinueLoop, LoopException {
                String line = ele.trim();
                if (!line.isBlank() && !line.startsWith("#")) {
                    String[] segs = Strings.split(line, false, false, ' ', '\t','　');
                    //   format += "# wangwu   王五　　　　　  wangwu123    wangwu@cangling.cn \r\n";
                    // segs must be     0       1                2           3  ==4
                    if (segs.length != 4) {
                        log.info("{}",Json.toJson(segs));
                        log.warn("line{} 不符合要求 {}", line,segs.length);
                    } else {
                        LdapNodeData item = new LdapNodeData();
                        item.setName(segs[0]);
                        item.setDn(request.getDn());
                        item.setFolder(false);
                        item.getObjectClasses().add("inetOrgPerson");
                        item.getObjectClasses().add("organizationalPerson");
                        item.getObjectClasses().add("person");
                        item.getObjectClasses().add("top");

                        List<LdapNodeAttribute> attributes = new ArrayList<>();

                        LdapNodeAttribute attr = new LdapNodeAttribute();
                        attr.setKey("description");
                        attr.setValue(segs[1]);
                        attr.setKind(AttributeKind.AK_STRING.getKind());
                        attr.setSysData(false);
                        attributes.add(attr);

                        attr = new LdapNodeAttribute();
                        attr.setKey("displayName");
                        attr.setValue(segs[1]);
                        attr.setKind(AttributeKind.AK_STRING.getKind());
                        attr.setSysData(false);
                        attributes.add(attr);

                        attr = new LdapNodeAttribute();
                        attr.setKey("uid");
                        attr.setValue(segs[0]);
                        attr.setKind(AttributeKind.AK_STRING.getKind());
                        attr.setSysData(false);
                        attributes.add(attr);

                        attr = new LdapNodeAttribute();
                        attr.setKey("mail");
                        attr.setValue(segs[3]);
                        attr.setKind(AttributeKind.AK_STRING.getKind());
                        attr.setSysData(false);
                        attributes.add(attr);

                        attr = new LdapNodeAttribute();
                        attr.setKey("userPassword");
                        attr.setValue(segs[2]);
                        attr.setKind(AttributeKind.AK_PASSWORD.getKind());
                        attr.setSysData(false);
                        attributes.add(attr);

                        attr = new LdapNodeAttribute();
                        attr.setKey("sn");
                        attr.setValue(segs[1].substring(0, 1));
                        attr.setKind(AttributeKind.AK_STRING.getKind());
                        attr.setSysData(false);
                        attributes.add(attr);

                        item.setAttributes(attributes);
                        users.add(item);
                    }
                }
            }
        });

        if (users.isEmpty()) {
            return BizResult.error(500, "没有解析到成功的用户信息，检查格式");
        }

        for (LdapNodeData item : users) {
            BizResult<LdapNodeData> ldapEntry = ldapService.createLdapEntry(item);
        }

        return BizResult.success(new ImportLdapExcelResponse());
    }
}
