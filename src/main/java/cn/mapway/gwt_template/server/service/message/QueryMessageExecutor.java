package cn.mapway.gwt_template.server.service.message;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.MailboxEntity;
import cn.mapway.gwt_template.shared.rpc.message.QueryMessageRequest;
import cn.mapway.gwt_template.shared.rpc.message.QueryMessageResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.pager.Pager;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryMessageExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryMessageExecutor extends AbstractBizExecutor<QueryMessageResponse, QueryMessageRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<QueryMessageResponse> process(BizContext context, BizRequest<QueryMessageRequest> bizParam) {
        QueryMessageRequest request = bizParam.getData();
        log.info("QueryMessageExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        Pager pager = new Pager();

        if (request.getPage() == null || request.getPage() < 1) {
            request.setPage(1);
        }
        pager.setPageNumber(request.getPage());

        if (request.getPageSize() == null || request.getPageSize() < 5) {
            request.setPageSize(20);
        }
        pager.setPageSize(request.getPageSize());

        QueryMessageResponse response = new QueryMessageResponse();
        response.setPage(pager.getPageNumber());
        response.setPageSize(pager.getPageSize());

        if (request.getQueryPublicMessage() != null && request.getQueryPublicMessage()) {
            //查询公共消息
            Cnd where = Cnd.where(MailboxEntity.FLD_TO_USER_ID, "=", -1L);
            where.desc(MailboxEntity.FLD_CREATE_TIME);
            int count = dao.count(MailboxEntity.class, where);
            List<MailboxEntity> list = dao.query(MailboxEntity.class, where, pager);
            response.setTotal(count);
            response.setMailboxes(list);
        } else {
            Cnd where = Cnd.where(MailboxEntity.FLD_TO_USER_ID, "=", user.getUser().getUserId());
            where.desc(MailboxEntity.FLD_CREATE_TIME);
            int count = dao.count(MailboxEntity.class, where);
            List<MailboxEntity> list = dao.query(MailboxEntity.class, where, pager);
            response.setTotal(count);
            response.setMailboxes(list);
        }
        return BizResult.success(response);
    }
}
