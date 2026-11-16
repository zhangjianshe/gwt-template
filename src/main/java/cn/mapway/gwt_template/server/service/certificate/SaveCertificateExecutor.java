package cn.mapway.gwt_template.server.service.certificate;
import cn.mapway.biz.core.*;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.CanglingCertificateEntity;
import cn.mapway.gwt_template.shared.rpc.certificate.*;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.sql.Timestamp;
@Component public class SaveCertificateExecutor extends AbstractBizExecutor<SaveCertificateResponse, SaveCertificateRequest> {
    @Resource Dao dao;
    @Override protected BizResult<SaveCertificateResponse> process(BizContext context, BizRequest<SaveCertificateRequest> param) {
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertNotNull(user, "请先登录"); assertNotNull(user.getUser(), "请先登录");

        assertNotNull(param.getData(), "证书参数不能为空");
        CanglingCertificateEntity input = param.getData().getCertificate();
        assertNotNull(input, "证书不能为空");
        assertTrue(Strings.isNotBlank(input.getName()) && input.getName().trim().length() <= 256, "证书名称必须为 1-256 个字符");
        assertTrue(Strings.isNotBlank(input.getPublicKey()), "公钥不能为空");
        String[] parts = input.getPublicKey().trim().split("\\s+");
        assertTrue(parts.length >= 2 && "ssh-ed25519".equals(parts[0]), "仅支持 Ed25519 公钥");
        String publicKey = parts[0] + " " + parts[1];
        assertTrue(publicKey.length() <= 512, "公钥过长");
        assertTrue(Strings.isNotBlank(input.getPrivateKey()) && input.getPrivateKey().length() <= 32768, "私钥为空或过长");
        // Lookup and assignment are scoped to the authenticated account; ignore client IDs/user IDs.
        CanglingCertificateEntity existing = dao.fetch(CanglingCertificateEntity.class, Cnd.where("user_id", "=", user.getUser().getUserId()).and("public_key", "=", publicKey));
        Timestamp now = new Timestamp(System.currentTimeMillis());
        CanglingCertificateEntity saved = existing == null ? new CanglingCertificateEntity() : existing;
        saved.setName(input.getName().trim()); saved.setPublicKey(publicKey); saved.setPrivateKey(input.getPrivateKey());
        saved.setUserId(user.getUser().getUserId()); saved.setUpdateTime(now);
        if (existing == null) { saved.setId(R.UU16()); saved.setCreateTime(now); dao.insert(saved); }
        else { dao.update(saved); }
        SaveCertificateResponse response = new SaveCertificateResponse(); response.setCertificate(saved);
        return BizResult.success(response);

    }
}
