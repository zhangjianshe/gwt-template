package cn.mapway.gwt_template.server.service.project.res;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.server.service.file.FileService;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.file.*;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectPermission;
import cn.mapway.gwt_template.shared.rpc.project.res.ViewProjectFileRequest;
import cn.mapway.gwt_template.shared.rpc.project.res.ViewProjectFileResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * ViewProjectFileExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class ViewProjectFileExecutor extends AbstractBizExecutor<ViewProjectFileResponse, ViewProjectFileRequest> {
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<ViewProjectFileResponse> process(BizContext context, BizRequest<ViewProjectFileRequest> bizParam) {
        ViewProjectFileRequest request = bizParam.getData();
        log.info("ViewProjectFileExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        ViewProjectFileResponse response = new ViewProjectFileResponse();
        assertNotNull(Strings.isNotBlank(request.getResourceId()), "没有项目资源ID");
        assertNotNull(Strings.isNotBlank(request.getRelPathName()), "没有文件名称");
        ProjectPermission permission = projectService.findUserPermissionInProjectResource(user.getUser().getUserId(), request.getResourceId());
        assertTrue(permission.isSuper() || permission.canRead(), "没有权限浏览该数据");
        BizResult<String> resourceAbsolutePath = projectService.getResourceAbsolutePath(request.getResourceId());
        if (resourceAbsolutePath.isFailed()) {
            return resourceAbsolutePath.asBizResult();
        }
        String absolutePath = FileCustomUtils.concatPath(resourceAbsolutePath.getData(), request.getRelPathName());
        File file = new File(absolutePath);
        if (!file.exists()) {
            return BizResult.error(500, "目标资源不存在");
        }
        try {

            // 2. 处理 filePath：确保不以斜杠开头，避免拼接出双斜杠
            String cleanPath = request.getRelPathName();
            if (cleanPath.startsWith("/")) {
                cleanPath = cleanPath.substring(1);
            }

            // 3. 关键点：编码整个路径，然后将 %2F 还原为 /
            // 这样：'my docs/测试.docx' -> 'my%20docs/%E6%B5%8B%E8%AF%95.docx'
            String encodedPath = URLEncoder.encode(cleanPath, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20") // 转换空格为 %20 (更符合 URI 标准)
                    .replaceAll("%2F", "/");   // 还原路径分隔符

            // 4. 使用 concatPath 拼接
            String url = FileCustomUtils.concatPath("/api/v1/project/file", request.getResourceId(), encodedPath);
            String mimetype = Files.probeContentType(file.toPath());
            response.setMimeType(mimetype);
            String suffix = org.nutz.lang.Files.getSuffixName(file).toLowerCase();
            if (EditableFileSuffix.fromSuffix(suffix) != EditableFileSuffix.NONE) {
                response.setBody(Files.readString(file.toPath()));
            } else if (ImageFileSuffix.fromSuffix(suffix) != ImageFileSuffix.NONE) {
                response.setBody(url);
            } else if (OfficeFileSuffix.fromSuffix(suffix) != OfficeFileSuffix.NONE) {
                OfficeFileSuffix officeFileSuffix = OfficeFileSuffix.fromSuffix(suffix);
                switch (officeFileSuffix) {
                    case DOC:
                    case DOCX:
                        response.setMimeType(AppConstant.CANGLING_MIME_TYPE);
                        response.setBody(FileService.generateWordPreviewHtml(file, url));
                        break;
                    case PPT:
                    case PPTX:
                        response.setMimeType(AppConstant.CANGLING_MIME_TYPE);
                        response.setBody(FileService.generatePptPreviewHtml(file, url));
                        break;
                    case PDF:
                        response.setMimeType(AppConstant.CANGLING_MIME_FRAME);
                        response.setBody(url);
                        break;
                    case XLS:
                    case XLSX: {
                        response.setMimeType(AppConstant.CANGLING_MIME_TYPE);
                        response.setBody(FileService.previewExcel(file, url));
                    }
                    break;
                    default: {
                        response.setMimeType(AppConstant.CANGLING_MIME_TYPE);
                        response.setBody(generateOfficePlaceholderHtml(file, url));
                    }
                }
            } else if (CompressFileSuffix.fromSuffix(suffix) != CompressFileSuffix.NONE) {
                response.setMimeType(AppConstant.CANGLING_MIME_TYPE);
                response.setBody(listCompressListFiles(file, url));
            } else if (VideoFileSuffix.fromSuffix(suffix) != VideoFileSuffix.NONE) {
                response.setMimeType(AppConstant.CANGLING_MIME_TYPE);
                response.setBody(generateVideoHtml(url));
            } else if (AudioFileSuffix.fromSuffix(suffix) != AudioFileSuffix.NONE) {
                response.setMimeType(AppConstant.CANGLING_MIME_TYPE);
                response.setBody(generateAudioHtml(url));
            } else {
                response.setMimeType(AppConstant.CANGLING_MIME_TYPE);
                response.setBody(generateBinaryHtml(file, url));
            }
            response.setFileName(request.getRelPathName());
            response.setResourceId(request.getResourceId());
            response.setFileSize((double) file.length());
        } catch (IOException e) {
            return BizResult.error(500, e.getMessage());
        }
        return BizResult.success(response);
    }

    private String listCompressListFiles(File file, String url) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        sb.append("<div style='padding:15px; font-family: sans-serif;'>");

        // 顶部操作栏：包含下载整个压缩包的链接
        sb.append("<div style='margin-bottom:15px; display:flex; justify-content:space-between; align-items:center;'>");
        sb.append("<strong style='font-size:16px;'>📦 内容列表</strong>");
        sb.append("<a href='").append(url).append("' target='_blank' style='")
                .append("background-color: #28a745; color: white; padding: 8px 16px; ")
                .append("text-decoration: none; border-radius: 4px; font-weight: bold; font-size: 14px;")
                .append("'>下载压缩包</a>");
        sb.append("</div>");

        sb.append("<table style='width:100%; border-collapse: collapse; background: white; border: 1px solid #ddd;'>");
        sb.append("<tr style='background:#f8f9fa; text-align:left;'>");
        sb.append("<th style='border:1px solid #ddd; padding:12px;'>Name</th>");
        sb.append("<th style='border:1px solid #ddd; padding:12px; width:120px;'>Size</th>");
        sb.append("<th style='border:1px solid #ddd; padding:12px; width:180px;'>Modified</th>");
        sb.append("</tr>");

        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            int count = 0;
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                count++;

                // 限制显示数量，防止前端渲染超大列表时卡死
                if (count > 800) {
                    sb.append("<tr><td colspan='3' style='padding:12px; color:#999; text-align:center; background:#fffbe6;'>")
                            .append("... List truncated (Total items: ").append(zipFile.size()).append(") ...")
                            .append("</td></tr>");
                    break;
                }

                String name = entry.getName();
                String size = entry.isDirectory() ? "[DIR]" : formatFileSize(entry.getSize());
                String time = sdf.format(entry.getTime());

                sb.append("<tr style='border-bottom: 1px solid #eee;'>");
                // 使用 Strings.escapeHtml 转义文件名，防止特殊字符（如 <arch>）导致对齐失效或解析错误
                sb.append("<td style='padding:10px; border-right: 1px solid #eee; font-family: monospace;'>")
                        .append(Strings.escapeHtml(name)).append("</td>");
                sb.append("<td style='padding:10px; border-right: 1px solid #eee; color: #666;'>").append(size).append("</td>");
                sb.append("<td style='padding:10px; color: #666; font-size: 13px;'>").append(time).append("</td>");
                sb.append("</tr>");
            }
        } catch (IOException e) {
            log.error("Read zip error", e);
            return "<div style='color:red; padding:10px;'>Error reading zip file: " + Strings.escapeHtml(e.getMessage()) + "</div>";
        }

        sb.append("</table>");
        sb.append("</div>");
        return sb.toString();
    }

    // 辅助：格式化文件大小
    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new java.text.DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


    private String generateBinaryHtml(File file, String url) {
        try {
            // 读取前 4KB 即可，没必要加载整个大二进制文件到内存
            long readLen = Math.min(file.length(), 4096);
            byte[] buffer = new byte[(int) readLen];

            try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                fis.read(buffer);
            }

            String hexView = convertToHexDump(buffer);

            return "<div>" +
                    "<div style='margin-bottom:10px;'><strong>Binary Preview (Hex Dump):</strong> " +
                    "<a href='" + url + "' style='margin-left:20px;'>下载文件</a></div>" +
                    hexView +
                    "</div>";
        } catch (IOException e) {
            return "<div>Error generating hex dump: " + e.getMessage() + "</div>";
        }
    }

    /**
     * 将二进制数据转换为格式化的十六进制 dump HTML 字符串（确保完美对齐）。
     *
     * @param data 需要转换的二进制数据
     * @return 包含 CSS 样式的 <pre> 标签 HTML
     */
    private String convertToHexDump(byte[] data) {
        if (data == null || data.length == 0) return "<pre>Empty file</pre>";

        StringBuilder sb = new StringBuilder();

        // 1. 定义样式：强制等宽字体，禁用自动换行以防错位
        sb.append("<pre style='")
                .append("font-family: \"Courier New\", Courier, monospace; ")
                .append("font-size: 16px; ")
                .append("line-height: 1.5; ")
                .append("background: #f4f4f4; ")
                .append("padding: 15px; ")
                .append("color: #333; ")
                .append("white-space: pre; ") // 必须是 pre，不能是 pre-wrap
                .append("overflow-x: auto; ")
                .append("'>");

        int len = Math.min(data.length, 10240); // 限制预览前 10KB

        for (int i = 0; i < len; i += 16) {
            // A. 地址偏移量 (8位十六进制)
            sb.append(String.format("%08x  ", i));

            // B. 十六进制数据部分
            for (int j = 0; j < 16; j++) {
                if (i + j < len) {
                    sb.append(String.format("%02x ", data[i + j]));
                } else {
                    sb.append("   "); // 补齐空格
                }
                if (j == 7) sb.append(" "); // 中间分隔符
            }

            sb.append(" |");

            // C. ASCII 字符部分 (关键：必须转义 HTML 敏感字符)
            for (int j = 0; j < 16; j++) {
                if (i + j < len) {
                    int b = data[i + j] & 0xFF;
                    if (b >= 32 && b <= 126) {
                        char c = (char) b;
                        // 防止浏览器解析为标签
                        if (c == '<') sb.append("&lt;");
                        else if (c == '>') sb.append("&gt;");
                        else if (c == '&') sb.append("&amp;");
                        else sb.append(c);
                    } else {
                        sb.append(".");
                    }
                }
            }
            sb.append("|\n");
        }

        if (data.length > len) {
            sb.append("\n... [已截断，文件过大仅展示前 10KB]");
        }

        sb.append("</pre>");
        return sb.toString();
    }

    private String generateVideoHtml(String url) {
        //Added 'controls' so the user can actually play it, and fixed tags
        return "<div style='\n" +
                "    align-items: center;\n" +
                "    display: flex;\n" +
                "    justify-content: center;\n'>" +
                "<video src='" + url + "' controls style='max-width:100%;' />" +
                "</div>";
    }

    private String generateAudioHtml(String url) {
        String html = "<div style='\n" +
                "    align-items: center;\n" +
                "    display: flex;\n" +
                "    justify-content: center;\n'>" +
                "<audio src='" + url + "' />" +
                "<div>";
        return html;
    }

    private String generateOfficePlaceholderHtml(File file, String url) {
        return "<div style='text-align:center; padding:50px; background:#f9f9f9; border:1px dashed #ccc;'>" +
                "  <div style='font-size:48px; margin-bottom:20px;'>📄</div>" +
                "  <div style='font-size:18px; color:#333; margin-bottom:10px;'><b>" + file.getName() + "</b></div>" +
                "  <p style='color:#666;'>该文件类型暂不支持直接在线预览</p>" +
                "  <a href='" + url + "' style='display:inline-block; margin-top:15px; padding:10px 25px; " +
                "  background:#007bff; color:white; text-decoration:none; border-radius:4px; font-weight:bold;'>立即下载查看</a>" +
                "</div>";
    }
}
