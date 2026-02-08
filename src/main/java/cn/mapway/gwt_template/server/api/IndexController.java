package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.file.FileUploadExecutor;
import cn.mapway.gwt_template.server.service.git.GitRepoService;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.file.UploadFileRequest;
import cn.mapway.gwt_template.shared.rpc.file.UploadReturnResponse;
import cn.mapway.gwt_template.shared.rpc.user.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import org.nutz.json.Json;
import org.nutz.lang.Streams;
import org.nutz.lang.random.R;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class IndexController extends ApiBaseController{

    @Resource
    private GitRepoService gitRepoService;

    @Resource
    FileUploadExecutor fileUploadExecutor;
    @Resource
    ProjectService projectService;

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

    @RequestMapping(value = "/raw/{owner}/{projectName}/**", method = RequestMethod.GET)
    public void rawContent(@PathVariable String owner,
                           @PathVariable String projectName,
                           HttpServletRequest request,
                           HttpServletResponse response) throws IOException {

        BizContext bizContext = getBizContext();
        LoginUser user = (LoginUser) bizContext.get(AppConstant.KEY_LOGIN_USER);
        if(user==null)
        {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("请登录后获取内容");
            return;
        }
        CommonPermission permission = projectService.findUserPermissionInProjectByName(user.getUser().getUserId(), owner, projectName);
        if (!permission.isAdmin() && !permission.canRead()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("请登录后获取内容");
            return;
        }

        // Extract the path from the URI (everything after /raw/owner/projectName/)
        String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String prefix = "/raw/" + owner + "/" + projectName + "/";
        String gitPath = fullPath.substring(prefix.length());

        if (gitPath.endsWith(".png")) response.setContentType("image/png");
        else if (gitPath.endsWith(".jpg")) response.setContentType("image/jpeg");
        else if (gitPath.endsWith(".pdf")) response.setContentType("application/pdf");
        else response.setContentType("application/octet-stream");

        // Optional: Set filename for downloads
        String fileName = gitPath.contains("/") ? gitPath.substring(gitPath.lastIndexOf("/") + 1) : gitPath;
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");

        gitRepoService.writeFileContentToStream(owner, projectName, gitPath, response.getOutputStream());
    }
}
