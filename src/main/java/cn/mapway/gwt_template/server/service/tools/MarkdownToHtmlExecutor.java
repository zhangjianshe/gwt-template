package cn.mapway.gwt_template.server.service.tools;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.git.MarkdownService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.tools.MarkdownToHtmlRequest;
import cn.mapway.gwt_template.shared.rpc.tools.MarkdownToHtmlResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * MarkdownToHtmlExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class MarkdownToHtmlExecutor extends AbstractBizExecutor<MarkdownToHtmlResponse, MarkdownToHtmlRequest> {
    @Resource
    MarkdownService markdownService;

    @Override
    protected BizResult<MarkdownToHtmlResponse> process(BizContext context, BizRequest<MarkdownToHtmlRequest> bizParam) {
        MarkdownToHtmlRequest request = bizParam.getData();
        log.info("MarkdownToHtmlExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        MarkdownToHtmlResponse response = new MarkdownToHtmlResponse();
        if (Strings.isBlank(request.getMarkdown())) {
            response.setHtml("<div></div>");
        } else {
            String html = markdownService.renderHtml(request.getMarkdown());
            response.setHtml(html);
        }
        return BizResult.success(response);
    }
}
