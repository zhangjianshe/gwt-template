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
@Component public class ListCertificateExecutor extends AbstractBizExecutor<CertificateListResponse, CertificateListRequest> {
    @Resource Dao dao;
    @Override protected BizResult<CertificateListResponse> process(BizContext context, BizRequest<CertificateListRequest> param) {
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertNotNull(user, "请先登录"); assertNotNull(user.getUser(), "请先登录");

        CertificateListResponse response = new CertificateListResponse();
        response.setServerVersion(cn.mapway.gwt_template.server.service.tools.Versions.getVersion().trim());
        response.setCertificates(dao.query(CanglingCertificateEntity.class, Cnd.where("user_id", "=", user.getUser().getUserId()).asc("name")));
        return BizResult.success(response);

    }
}
