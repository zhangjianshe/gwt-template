package cn.mapway.gwt_template.server.service.certificate;

import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.CanglingCertificateEntity;
import cn.mapway.gwt_template.shared.rpc.certificate.CertificateListRequest;
import cn.mapway.gwt_template.shared.rpc.certificate.PublicCertificateListResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.nutz.dao.Dao;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

/** Run with assertions enabled, like the other standalone server regression tests. */
public class ListPublicCertificateTest {
    public static void main(String[] args) throws Exception {
        ListPublicCertificateExecutor executor = new ListPublicCertificateExecutor();
        AtomicInteger queries = new AtomicInteger();
        executor.dao = (Dao) Proxy.newProxyInstance(Dao.class.getClassLoader(), new Class[]{Dao.class},
                (proxy, method, values) -> {
                    if (!method.getName().equals("query")) throw new AssertionError(method.getName());
                    queries.incrementAndGet();
                    assert values[0] == CanglingCertificateEntity.class;
                    String condition = values[1].toString();
                    assert condition.contains("user_id") && condition.contains("20261116") : condition;
                    CanglingCertificateEntity row = new CanglingCertificateEntity();
                    row.setId("cert-1"); row.setName("Windows Keeper");
                    row.setPublicKey("ssh-ed25519 public"); row.setPrivateKey("DO-NOT-RETURN-PRIVATE");
                    return Collections.singletonList(row);
                });
        BizContext context = new BizContext();
        try {
            executor.process(context, BizRequest.wrap("", new CertificateListRequest()));
            throw new AssertionError("Anonymous access must fail");
        } catch (RuntimeException expected) {
            assert queries.get() == 0;
        }
        RbacUserEntity user = new RbacUserEntity(); user.setUserId(20261116L);
        context.put(AppConstant.KEY_LOGIN_USER, new LoginUser(user));
        PublicCertificateListResponse result = executor.process(context,
                BizRequest.wrap("", new CertificateListRequest())).getData();
        String json = new ObjectMapper().writeValueAsString(result);
        assert queries.get() == 1;
        assert json.contains("Windows Keeper") && json.contains("publicKey");
        assert !json.contains("privateKey") && !json.contains("DO-NOT-RETURN-PRIVATE");
        System.out.println("Public certificate checks passed: login required, account-scoped query, no private key serialization.");
    }
}
