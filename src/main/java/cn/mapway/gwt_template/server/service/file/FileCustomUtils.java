package cn.mapway.gwt_template.server.service.file;

import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.file.*;
import cn.mapway.gwt_template.shared.rpc.project.module.PreviewData;
import lombok.extern.slf4j.Slf4j;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @Author baoshuaiZealot@163.com  2023/10/30
 */
@Slf4j
public class FileCustomUtils {
    /**
     * 读取文件Magic Number 取
     *
     * @param location
     * @param size
     * @return
     */
    public static byte[] readMagic(String location, int size) {
        byte[] data = new byte[size];
        int readSize = org.nutz.lang.Files.readRange(new File(location), 0, data, 0, size);
        return data;
    }

    public static List<String> splitIgnoreBlank(String s, String regex) {
        if (s == null || regex == null) {
            return Collections.emptyList();
        }
        String[] parts = s.split(regex);
        List<String> list = new LinkedList<>();
        for (String part : parts) {
            if (!Strings.isBlank(part)) {
                list.add(part.trim());
            }
        }
        return list;
    }

    /**
     * 连接路径
     *
     * @param paths
     * @return
     */
    public static String concatPath(String... paths) {
        if (paths == null || paths.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paths.length; i++) {
            String item = paths[i];
            if (item == null || item.length() == 0) {
                continue;
            }
            if (i > 0 && !item.startsWith("/")) {
                sb.append("/");
            }
            sb.append(item);
        }
        return sb.toString();
    }

    public static void setFilePermission(String file, String permission) {
        if (!Lang.isWin()) {
            try {
                // Define permissions (e.g., read/write/execute for owner only: rwx------)
                Set<PosixFilePermission> permissions = PosixFilePermissions.fromString(permission);

                // Create the directory with the specified permissions
                Files.setPosixFilePermissions(Path.of(file), permissions);

                log.info("Directory created successfully with specified permissions.");
            } catch (IOException e) {
                log.error("Failed to create directory: " + e.getMessage());
            }
        }
    }

    /**
     * 检查目录名称是否有效
     *
     * @param name
     * @return
     */
    public static BizResult<Boolean> checkName(String name) {
        if (Strings.isBlank(name)) {
            return BizResult.error(500, "目录名称无效");
        }
        if (name.length() > 64) {
            return BizResult.error(500, "目录名太长了,超过64个字符");
        }
        boolean matches = name.matches("[^\\s\\\\/:\\*\\?\\\"<>\\|](\\x20|[^\\s\\\\/:\\*\\?\\\"<>\\|])*[^\\s\\\\/:\\*\\?\\\"<>\\|\\.]$");
        if (!matches) {
            return BizResult.error(500, "目录名称无效" + name);
        }
        return BizResult.success(true);
    }

    public static File getFirstValidFile(File dir, FileFilter filter, boolean recursive) {
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        for (File file : files) {
            if (file.isDirectory() && recursive) {
                File targetFile = getFirstValidFile(file, filter, recursive);
                if (targetFile != null) {
                    return targetFile;
                }
            } else {
                if (filter.accept(file)) {
                    return file;
                }
            }
        }
        return null;
    }

    public static String numberToPath(Long number) {
        if (number == null) {
            throw new IllegalArgumentException("Number cannot be null");
        } else {
            return stringToPath(String.valueOf(number));
        }
    }

    public static String stringToPath(String str) {
        if (str != null && !str.isEmpty()) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < str.length(); i += 2) {
                int end = Math.min(i + 2, str.length());
                if (i > 0) {
                    sb.append("/");
                }

                sb.append(str, i, end);
            }

            return sb.toString();
        } else {
            return "";
        }
    }

    public static String uuidToPath(String uuid) {
        if (uuid != null && uuid.length() >= 2) {
            String cleanUuid = uuid.replace("-", "");
            if (cleanUuid.length() < 2) {
                throw new IllegalArgumentException("UUID after removing hyphens is too short");
            } else {
                String directory = cleanUuid.substring(0, 2);
                String file = cleanUuid.substring(2);
                return directory + "/" + file;
            }
        } else {
            throw new IllegalArgumentException("UUID must be non-null and at least 2 characters long");
        }
    }

    /**
     * 获取文件的创建时间
     *
     * @param file
     * @return
     */
    public static long getFileCreateTimeInMillis(File file) {
        if (!file.exists()) {
            return new Date().getTime();
        }
        try {
            BasicFileAttributes basicFileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            return basicFileAttributes.creationTime().toMillis();
        } catch (IOException e) {
            return file.lastModified();
        }
    }

    /**
     * 获取文件的创建时间
     *
     * @param file
     * @return
     */
    public static long getFileModifyTimeInMillis(File file) {
        if (!file.exists()) {
            return new Date().getTime();
        }
        try {
            BasicFileAttributes basicFileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            return basicFileAttributes.lastModifiedTime().toMillis();
        } catch (IOException e) {
            return file.lastModified();
        }
    }

    public static BasicFileAttributes readFileAttributes(File file) {
        if (!file.exists()) {
            return null;
        }
        try {
            BasicFileAttributes basicFileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            return basicFileAttributes;
        } catch (IOException e) {
            return null;
        }
    }

    public static String getFileName(String filePath) {
        if (filePath == null || filePath.equals("")) {
            return "";
        }
        int pos = filePath.lastIndexOf("/");
        if (pos == -1) {
            return filePath;
        }
        return filePath.substring(pos + 1);
    }

    public static String readFirstLine(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return null;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                return reader.readLine();
            }
        } catch (Exception e) {
            log.error("读取文件第一行失败: " + filePath, e);
            return null;
        }
    }


    public static String listCompressListFiles(File file, String url) {
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
    public static String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new java.text.DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


    public static String generateBinaryHtml(File file, String url) {
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
    public static String convertToHexDump(byte[] data) {
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

    public static String generateVideoHtml(String url) {
        //Added 'controls' so the user can actually play it, and fixed tags
        return "<div style='\n" +
                "    align-items: center;\n" +
                "    display: flex;\n" +
                "    justify-content: center;\n'>" +
                "<video src='" + url + "' controls style='max-width:100%;' />" +
                "</div>";
    }

    public static String generateAudioHtml(String url) {
        String html = "<div style='\n" +
                "    align-items: center;\n" +
                "    display: flex;\n" +
                "    justify-content: center;\n'>" +
                "<audio src='" + url + "' />" +
                "<div>";
        return html;
    }

    public static String generateOfficePlaceholderHtml(File file, String url) {
        return "<div style='text-align:center; padding:50px; background:#f9f9f9; border:1px dashed #ccc;'>" +
                "  <div style='font-size:48px; margin-bottom:20px;'>📄</div>" +
                "  <div style='font-size:18px; color:#333; margin-bottom:10px;'><b>" + file.getName() + "</b></div>" +
                "  <p style='color:#666;'>该文件类型暂不支持直接在线预览</p>" +
                "  <a href='" + url + "' style='display:inline-block; margin-top:15px; padding:10px 25px; " +
                "  background:#007bff; color:white; text-decoration:none; border-radius:4px; font-weight:bold;'>立即下载查看</a>" +
                "</div>";
    }

    public static PreviewData processFilePreviewData(File file, String url) throws IOException {
        PreviewData previewData = new PreviewData();
        String mimetype = Files.probeContentType(file.toPath());
        previewData.setMimeType(mimetype);
        String suffix = org.nutz.lang.Files.getSuffixName(file).toLowerCase();
        if (EditableFileSuffix.fromSuffix(suffix) != EditableFileSuffix.NONE) {
            previewData.setBody(Files.readString(file.toPath()));
        } else if (ImageFileSuffix.fromSuffix(suffix) != ImageFileSuffix.NONE) {
            previewData.setBody(url);
        } else if (OfficeFileSuffix.fromSuffix(suffix) != OfficeFileSuffix.NONE) {
            OfficeFileSuffix officeFileSuffix = OfficeFileSuffix.fromSuffix(suffix);
            switch (officeFileSuffix) {
                case DOC:
                case DOCX:
                    previewData.setMimeType(AppConstant.CANGLING_MIME_TYPE);
                    previewData.setBody(FileService.generateWordPreviewHtml(file, url));
                    break;
                case PPT:
                case PPTX:
                    previewData.setMimeType(AppConstant.CANGLING_MIME_TYPE);
                    previewData.setBody(FileService.generatePptPreviewHtml(file, url));
                    break;
                case PDF:
                    previewData.setMimeType(AppConstant.CANGLING_MIME_FRAME);
                    previewData.setBody(url);
                    break;
                case XLS:
                case XLSX: {
                    previewData.setMimeType(AppConstant.CANGLING_MIME_TYPE);
                    previewData.setBody(FileService.previewExcel(file, url));
                }
                break;
                default: {
                    previewData.setMimeType(AppConstant.CANGLING_MIME_TYPE);
                    previewData.setBody(FileCustomUtils.generateOfficePlaceholderHtml(file, url));
                }
            }
        } else if (CompressFileSuffix.fromSuffix(suffix) != CompressFileSuffix.NONE) {
            previewData.setMimeType(AppConstant.CANGLING_MIME_TYPE);
            previewData.setBody(FileCustomUtils.listCompressListFiles(file, url));
        } else if (VideoFileSuffix.fromSuffix(suffix) != VideoFileSuffix.NONE) {
            previewData.setMimeType(AppConstant.CANGLING_MIME_TYPE);
            previewData.setBody(FileCustomUtils.generateVideoHtml(url));
        } else if (AudioFileSuffix.fromSuffix(suffix) != AudioFileSuffix.NONE) {
            previewData.setMimeType(AppConstant.CANGLING_MIME_TYPE);
            previewData.setBody(FileCustomUtils.generateAudioHtml(url));
        } else {
            previewData.setMimeType(AppConstant.CANGLING_MIME_TYPE);
            previewData.setBody(FileCustomUtils.generateBinaryHtml(file, url));
        }
        previewData.setFileName(org.nutz.lang.Files.getName(file));
        previewData.setFileSize((double) file.length());
        return previewData;
    }
}
