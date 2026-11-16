package cn.mapway.gwt_template.server.service.certificate;
import cn.mapway.biz.core.*;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.CanglingCertificateEntity;
import cn.mapway.gwt_template.shared.rpc.certificate.*;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
@Component public class ListPublicCertificateExecutor extends AbstractBizExecutor<PublicCertificateListResponse, CertificateListRequest> {
    @Resource Dao dao;
    @Override protected BizResult<PublicCertificateListResponse> process(BizContext context, BizRequest<CertificateListRequest> param) {
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertNotNull(user, "请先登录"); assertNotNull(user.getUser(), "请先登录");
        List<CanglingCertificateEntity> rows = dao.query(CanglingCertificateEntity.class,
                Cnd.where("user_id", "=", user.getUser().getUserId()).asc("name"));
        List<PublicCertificate> items = new ArrayList<>();
        for (CanglingCertificateEntity row : rows) {
            PublicCertificate item = new PublicCertificate();
            item.setId(row.getId()); item.setName(row.getName()); item.setPublicKey(row.getPublicKey());
            items.add(item);
        }
        PublicCertificateListResponse response = new PublicCertificateListResponse();
        response.setCertificates(items);
        return BizResult.success(response);
    }
}
