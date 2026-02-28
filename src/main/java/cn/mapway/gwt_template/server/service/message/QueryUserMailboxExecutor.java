package cn.mapway.gwt_template.server.service.message;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.MailboxEntity;
import cn.mapway.gwt_template.shared.rpc.message.QueryUserMailboxRequest;
import cn.mapway.gwt_template.shared.rpc.message.QueryUserMailboxResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryUserMailboxExecutor
 * 查询用户的所有消息人员列表
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryUserMailboxExecutor extends AbstractBizExecutor<QueryUserMailboxResponse, QueryUserMailboxRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<QueryUserMailboxResponse> process(BizContext context, BizRequest<QueryUserMailboxRequest> bizParam) {
        QueryUserMailboxRequest request = bizParam.getData();
        log.info("QueryUserMailboxExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        if (Strings.isNotBlank(request.getMailboxId())) {
            MailboxEntity fetch = dao.fetch(MailboxEntity.class, request.getMailboxId());
            if (fetch == null) {
                return BizResult.error(500, "no mailbox found " + request.getMailboxId());
            }
            QueryUserMailboxResponse response = new QueryUserMailboxResponse();
            response.setMailboxes(Lang.list(fetch));
            return BizResult.success(response);
        } else {
            List<MailboxEntity> list = dao.query(MailboxEntity.class,
                    Cnd.where(MailboxEntity.FLD_TO_USER_ID, "=", user.getUser().getUserId())
                            .or(MailboxEntity.FLD_FROM_USER_ID, "=", user.getUser().getUserId())
                            .or(MailboxEntity.FLD_IS_PUBLIC, "=", true).desc(MailboxEntity.FLD_CREATE_TIME));
            QueryUserMailboxResponse response = new QueryUserMailboxResponse();
            response.setMailboxes(list);
            return BizResult.success(response);
        }
    }
}
