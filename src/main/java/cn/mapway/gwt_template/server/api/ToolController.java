package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.tools.MarkdownToHtmlExecutor;
import cn.mapway.gwt_template.shared.rpc.tools.MarkdownToHtmlRequest;
import cn.mapway.gwt_template.shared.rpc.tools.MarkdownToHtmlResponse;
import cn.mapway.ui.shared.rpc.RpcResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 工具类
 */
@Doc(value = "工具", group = "其他")
@RestController
@RequestMapping("/api/v1/tools")
public class ToolController extends ApiBaseController {
    @Resource
    MarkdownToHtmlExecutor markdownToHtmlExecutor;

    /**
     * MarkdownToHtml
     *
     * @param request request
     * @return data
     */
    @Doc(value = "MarkdownToHtml", retClazz = {MarkdownToHtmlResponse.class})
    @RequestMapping(value = "/markdownToHtml", method = RequestMethod.POST)
    public RpcResult<MarkdownToHtmlResponse> markdownToHtml(@RequestBody MarkdownToHtmlRequest request) {
        BizResult<MarkdownToHtmlResponse> bizResult = markdownToHtmlExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
}
