package cn.mapway.gwt_template.server.api;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.config.SystemConfigService;
import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.server.service.file.FileRangeDownload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Serves software-set binaries under {@code /software/**} with HTTP Range so
 * cangling-keeper can resume large downloads.
 */
@Doc(value = "软件文件下载", group = "软件")
@Controller
public class SoftwareFileController {

    @Resource
    SystemConfigService systemConfigService;

    @RequestMapping(value = "/software/**", method = {RequestMethod.GET, RequestMethod.HEAD})
    public void download(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String fullPath = (String) req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        if (fullPath == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String prefix = "/software/";
        int idx = fullPath.indexOf(prefix);
        String rel = idx >= 0 ? fullPath.substring(idx + prefix.length()) : "";
        rel = URLDecoder.decode(rel, StandardCharsets.UTF_8);
        if (rel.trim().isEmpty() || rel.contains("..")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().println("request path error");
            return;
        }

        File root = new File(FileCustomUtils.concatPath(systemConfigService.getUploadRoot(), "software"));
        File target = new File(FileCustomUtils.concatPath(root.getAbsolutePath(), rel));
        File rootCanon = root.getCanonicalFile();
        File targetCanon = target.getCanonicalFile();
        if (!targetCanon.getPath().startsWith(rootCanon.getPath() + File.separator)
                && !targetCanon.equals(rootCanon)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().println("request path error");
            return;
        }

        String filename = URLEncoder.encode(target.getName(), StandardCharsets.UTF_8).replace("+", "%20");
        String disposition = "inline; filename=\"" + filename + "\"";
        FileRangeDownload.send(target, req, resp, null, disposition, "public, max-age=31536000, no-transform");
    }
}
