package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.file.FileUploadExecutor;
import cn.mapway.gwt_template.shared.rpc.file.UploadFileRequest;
import cn.mapway.gwt_template.shared.rpc.file.UploadReturnResponse;
import org.nutz.json.Json;
import org.nutz.lang.Streams;
import org.nutz.lang.random.R;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class IndexController {

    @Resource
    FileUploadExecutor fileUploadExecutor;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("timestamp", R.UU16());
        return "index";
    }

    /**
     * 主要用于上传文件图标
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/fileUpload", method = RequestMethod.POST)
    public void uploadFile(UploadFileRequest request, HttpServletResponse response) throws IOException {
        BizResult<UploadReturnResponse> execute = fileUploadExecutor.execute(null, BizRequest.wrap("", request));
        if (execute.isSuccess()) {
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            Streams.writeAndClose(response.getOutputStream(), Json.toJson(execute.getData()).getBytes());
        }

        UploadReturnResponse r = new UploadReturnResponse();
        r.setRetCode(execute.getCode());
        r.setMsg(execute.getMessage());
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        Streams.writeAndClose(response.getOutputStream(), Json.toJson(execute.getData()).getBytes());
    }
}
