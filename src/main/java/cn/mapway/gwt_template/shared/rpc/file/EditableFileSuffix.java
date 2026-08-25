package cn.mapway.gwt_template.shared.rpc.file;

import cn.mapway.ace.client.AceEditorMode;
import cn.mapway.ui.client.fonts.Fonts;
import lombok.Getter;

public enum EditableFileSuffix {

    SHELL("sh", AceEditorMode.SH, Fonts.SHELL),
    SQL("sql", AceEditorMode.SQL, Fonts.SQL),
    XML("xml", AceEditorMode.XML, Fonts.XML),
    JSON("json", AceEditorMode.JSON, Fonts.JSON),
    C("c", AceEditorMode.C_CPP, Fonts.CODE),
    CPP("cpp", AceEditorMode.C_CPP, Fonts.CODE),
    HPP("hpp", AceEditorMode.C_CPP, Fonts.CODE),
    TXT("txt", AceEditorMode.PLAIN_TEXT, Fonts.TXT),
    HTML("html", AceEditorMode.HTML, Fonts.HTML),
    PYTHON("py", AceEditorMode.PYTHON, Fonts.PYTHON),
    PHP("php", AceEditorMode.PHP, Fonts.TXT),
    YAML("yaml", AceEditorMode.YAML, Fonts.YAML),
    YML("yml", AceEditorMode.YAML, Fonts.YML),
    TOML("toml", AceEditorMode.TOML, Fonts.TXT),
    JAVA("java", AceEditorMode.JAVA, Fonts.JAVA),
    MARKDOWN("md", AceEditorMode.MARKDOWN, Fonts.TXT),
    JAVASCRIPT("js", AceEditorMode.JAVASCRIPT, Fonts.JAVASCRIPT),

    // --- 新增常见的配置文件与环境变量支持 ---
    ENV("env", AceEditorMode.SH, Fonts.TXT),                   // .env / .env.production
    CONF("conf", AceEditorMode.NGINX, Fonts.TXT),              // nginx.conf / redis.conf
    PROPERTIES("properties", AceEditorMode.PROPERTIES, Fonts.TXT), // application.properties
    INI("ini", AceEditorMode.INI, Fonts.TXT),                  // php.ini / config.ini
    LOG("log", AceEditorMode.PLAIN_TEXT, Fonts.LOGFILE),           // system.log
    DOCKERFILE("dockerfile", AceEditorMode.DOCKERFILE, Fonts.DOCKERFILE), // Dockerfile

    NONE("", AceEditorMode.TEXT, Fonts.FILE);


    @Getter
    String suffix;
    @Getter
    AceEditorMode mode;
    @Getter
    String unicode;

    EditableFileSuffix(String suffix, AceEditorMode mode, String unicode) {
        this.suffix = suffix;
        this.mode = mode;
        this.unicode = unicode;
    }

    /**
     * 根据文件名/后缀以及文件首行内容综合判断类型
     *
     * @param suffixOrFileName 文件名或后缀
     * @param firstLine        文件第一行内容（可以传入 null）
     */
    public static EditableFileSuffix fromContentOrSuffix(String suffixOrFileName, String firstLine) {
        // 1. 优先校验 Shebang (#!/...)
        if (firstLine != null && firstLine.trim().startsWith("#!/")) {
            String line = firstLine.toLowerCase();
            if (line.contains("python")) {
                return PYTHON;
            } else if (line.contains("node") || line.contains("javascript")) {
                return JAVASCRIPT;
            } else if (line.contains("php")) {
                return PHP;
            } else if (line.contains("bash") || line.contains("sh") || line.contains("zsh")) {
                return SHELL;
            }
            // 默认只要带 #!/ 的绝大多数属于 Shell / 可执行脚本
            return SHELL;
        }

        // 2. 回退到标准后缀/文件名判定
        return fromSuffix(suffixOrFileName);
    }

    /**
     * 根据后缀或文件名进行匹配
     *
     * @param suffixOrFileName 传入后缀名 (如 "conf") 或完整文件名/带点的后缀 (如 ".env", "nginx.conf", "Dockerfile")
     */
    public static EditableFileSuffix fromSuffix(String suffixOrFileName) {
        if (suffixOrFileName == null || suffixOrFileName.trim().isEmpty()) {
            return NONE;
        }

        String input = suffixOrFileName.toLowerCase().trim();

        // 1. 规整输入：如果传入的是带有前导点或全路径的字符串 (例如 ".env" 或 "/etc/nginx/nginx.conf")
        String cleanInput = input;
        int lastSlash = cleanInput.lastIndexOf("/");
        if (lastSlash != -1) {
            cleanInput = cleanInput.substring(lastSlash + 1);
        }

        // 2. 特殊文件名全匹配判断
        if ("dockerfile".equals(cleanInput)) {
            return DOCKERFILE;
        }
        if ("env".equals(cleanInput) || cleanInput.startsWith(".env") || cleanInput.startsWith("env.")) {
            return ENV;
        }

        // 3. 如果包含点，截取最后一个点后的后缀 (例如 "nginx.conf" -> "conf")
        String targetSuffix = cleanInput;
        if (cleanInput.contains(".")) {
            targetSuffix = cleanInput.substring(cleanInput.lastIndexOf(".") + 1);
        }

        // 4. 标准后缀遍历匹配
        for (EditableFileSuffix mode : EditableFileSuffix.values()) {
            if (mode != NONE && mode.suffix.equalsIgnoreCase(targetSuffix)) {
                return mode;
            }
        }

        return NONE;
    }
}