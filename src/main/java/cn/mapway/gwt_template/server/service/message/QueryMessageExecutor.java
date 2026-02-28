package cn.mapway.gwt_template.server.service.message;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.git.MarkdownService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.MailboxMessageEntity;
import cn.mapway.gwt_template.shared.rpc.message.QueryMessageRequest;
import cn.mapway.gwt_template.shared.rpc.message.QueryMessageResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.pager.Pager;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
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
    @Resource
    MarkdownService markdownService;

    @Override
    protected BizResult<QueryMessageResponse> process(BizContext context, BizRequest<QueryMessageRequest> bizParam) {
        QueryMessageRequest request = bizParam.getData();
        log.info("QueryMessageExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        //只获取一个消息 TODO 需要添加权限认证
        if (Strings.isNotBlank(request.getMessageId())) {
            MailboxMessageEntity fetch = dao.fetch(MailboxMessageEntity.class, Cnd.where(MailboxMessageEntity.FLD_ID, "=", request.getMessageId()));
            QueryMessageResponse response = new QueryMessageResponse();
            response.setMessageList(Lang.list(fetch));
            return BizResult.success(response);
        }

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

        assertTrue(Strings.isNotBlank(request.getMailboxId()), "没有绘画ID");

        Cnd where = Cnd.where(MailboxMessageEntity.FLD_MAILBOX_ID, "=", request.getMailboxId());
        where.desc(MailboxMessageEntity.FLD_CREATE_TIME);
        int count = dao.count(MailboxMessageEntity.class, where);
        List<MailboxMessageEntity> list = dao.query(MailboxMessageEntity.class, where, pager);
        response.setTotal(count);
        response.setMessageList(list);
        //处理内容类型
        for (MailboxMessageEntity entity : response.getMessageList()) {
            if ("text/markdown".equals(entity.getMimeType())) {
                entity.setBody(markdownService.renderHtml(entity.getBody()));
            }
        }
        return BizResult.success(response);
    }
}
