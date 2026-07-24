package cn.mapway.gwt_template.client.docker;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DockerAppEntity;
import cn.mapway.gwt_template.shared.rpc.docker.*;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.AiTextBox;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.client.widget.tree.Tree;
import cn.mapway.ui.client.widget.tree.TreeItem;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import cn.mapway.xterm.client.Terminal;
import cn.mapway.xterm.client.TerminalOptions;
import cn.mapway.xterm.client.TerminalPanel;
import cn.mapway.xterm.client.addon.AttachAddon;
import cn.mapway.xterm.client.addon.AttachOptions;
import cn.mapway.xterm.client.theme.Theme;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import elemental2.dom.CSSStyleDeclaration;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.EventSource;

public class DockerAppOperatorPanel extends CommonEventComposite implements RequiresResize, IData<DockerAppEntity> {

    private static final DockerAppOperatorPanelUiBinder ourUiBinder = GWT.create(DockerAppOperatorPanelUiBinder.class);
    private static Dialog<DockerAppOperatorPanel> dialog;
    private final Terminal console;
    @UiField
    Label lbSize;
    @UiField
    SaveBar saveBar;
    @UiField
    TerminalPanel terminal;
    @UiField
    DockLayoutPanel root;
    @UiField
    Tree list;
    @UiField
    SStyle style;
    @UiField
    AiButton btnExec;
    @UiField
    AiTextBox txtCommand;
    @UiField
    AiButton btnStartService;
    @UiField
    AiButton btnStop;
    @UiField
    AiButton btnRestart;
    @UiField
    AiButton lbTitle;
    AttachAddon currentAttach = null;
    private DockerAppEntity appEntity;
    private EventSource activeLogStream;
    private String currentSelectedService;
    private elemental2.dom.WebSocket terminalWebSocket;

    public DockerAppOperatorPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));

        // 1. 初始化 Terminal 配置
        TerminalOptions options = new TerminalOptions.Builder().setAllowProposedApi(false).setAllowTransparency(false).setCursorBlink(true).build();

        // 调整字号：从 13 改为 16（或者 18，字号更大更清晰）
        options.fontSize = 16;
        options.enableWebgl = false;
        options.allowMouseEvents = true;

        // 2. 配置白色背景（Light Theme）主题
        Theme theme = new Theme();
        theme.background = "#ffffff"; // 背景色：纯白
        theme.foreground = "#24292e"; // 前景色：深灰/黑字（比纯黑更护眼）
        theme.selection = "#c8c8c8";  // 选中文本背景色
        theme.cursor = "#000000";     // 光标颜色：黑色

        // 如果 CSS 变量里没有定义，优先使用上面的白色主题配置
        applyThemeFromCss(theme);

        console = terminal.create(options);
        console.setTheme(theme);
        console.reset();
        showSaveBar(false);
    }

    public static Dialog<DockerAppOperatorPanel> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        }
        return createOne();
    }

    private static Dialog<DockerAppOperatorPanel> createOne() {
        DockerAppOperatorPanel panel = new DockerAppOperatorPanel();
        panel.showSaveBar(true);
        return new Dialog<>(panel, "Docker 控制台");
    }

    private void showSaveBar(boolean b) {
        if (b) {
            root.setWidgetSize(saveBar, 60);
        } else {
            root.setWidgetSize(saveBar, 0);
        }
    }

    /**
     * 使用 Elemental2 读取 CSS 变量，完全代替原先的 JSNI
     */
    private void applyThemeFromCss(Theme theme) {
        try {
            Element rootEl = DomGlobal.document.documentElement;
            CSSStyleDeclaration rootStyles = getComputedStyle(rootEl);

            String bg = rootStyles.getPropertyValue("--terminal-background-color");
            String fg = rootStyles.getPropertyValue("--terminal-text-color");

            // 只有当 CSS 显式定义了变量时才覆盖，否则保留上面设置的 #ffffff 白底黑字
            if (StringUtil.isNotBlank(bg)) {
                theme.background = bg.trim();
            }
            if (StringUtil.isNotBlank(fg)) {
                theme.foreground = fg.trim();
            }
        } catch (Exception ignored) {
            // 忽略异常，使用默认白色主题
        }
    }

    private native CSSStyleDeclaration getComputedStyle(Element element)/*-{
        return window.getComputedStyle(element);
    }-*/;

    @Override
    public Size requireDefaultSize() {
        return ClientContext.getDialogSize();
    }

    @Override
    public DockerAppEntity getData() {
        return appEntity;
    }

    @Override
    public void setData(DockerAppEntity obj) {
        this.appEntity = obj;
        toUI();
    }

    private void toUI() {
        stopLogStream();
        currentSelectedService = null;

        if (appEntity == null) {
            return;
        }

        QueryDockerAppInfoRequest request = new QueryDockerAppInfoRequest();
        request.setDockerAppId(appEntity.getId());

        AppProxy.get().queryDockerAppInfo(request, new AsyncCallback<RpcResult<QueryDockerAppInfoResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                saveBar.msg(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryDockerAppInfoResponse> result) {
                if (result.isSuccess()) {
                    console.clear();
                    QueryDockerAppInfoResponse data = result.getData();
                    lbSize.setText("\uD83D\uDDB4" + (StringUtil.isNotBlank(data.getTotalSize()) ? data.getTotalSize() : "-"));

                    if (StringUtil.isNotBlank(data.getStatus())) {
                        console.write(normalizeLineBreaks(data.getStatus()));
                    }

                    if (data.getErrors() != null && !data.getErrors().isEmpty()) {
                        for (String err : data.getErrors()) {
                            console.writeln(Ansi.error("[WARN/ERR] " + err));
                        }
                    }

                    // 渲染服务列表树
                    list.clear();
                    if (data.getServices() != null) {
                        for (String name : data.getServices()) {
                            TreeItem treeItem = list.addItem(null, name, "");
                            treeItem.setData(name);
                        }
                    }

                    setControlToolsVisible(false);
                } else {
                    saveBar.msg(result.getMessage());
                }
            }
        });
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isClose()) {
            stopLogStream();
            fireEvent(event);
        }
    }

    @UiHandler("list")
    public void listCommon(CommonEvent event) {
        if (event.isSelect()) {
            TreeItem item = event.getValue();
            if (item == null || item.getData() == null) {
                return;
            }
            String serviceName = item.getData().toString();
            if (serviceName.equals(currentSelectedService)) {
                return; // 避免重复点击相同的 service 重新建立连接
            }

            this.currentSelectedService = serviceName;

            btnStartService.setText("重启(" + serviceName + ")");
            btnStartService.setData(serviceName);
            btnExec.setText("调式容器(" + serviceName + ")");
            setControlToolsVisible(true);

            // 连接并跟踪服务实时日志
            connectServiceLogs(serviceName);
        }
    }

    /**
     * 连接 Docker 服务日志流 (SSE)
     */
    private void connectServiceLogs(String serviceName) {
        stopLogStream();

        // 1. 彻底清空终端（包含 Scrollback 历史记录与光标归位）
        console.write("\033[2J\033[3J\033[H");
        console.writeln(Ansi.warn("[SYS] 正在连接服务 [" + serviceName + "] 的实时日志...\r\n"));

        // 2. 拼接 SSE URL (确保 appId 传递正确)
        String sseUrl = GWT.getHostPageBaseURL() + "api/v1/docker/stream?" + "appId=" + appEntity.getId() + "&service=" + serviceName + "&tail=200";

        try {
            activeLogStream = new EventSource(sseUrl);

            activeLogStream.onmessage = evt -> {
                if (evt.data != null) {
                    // 将 Linux 换行符转换为 Xterm 要求的 \r\n
                    String msg = evt.data.replaceAll("\r?\n", "\r\n");
                    console.writeln(msg);
                }
            };

            activeLogStream.onerror = evt -> {
                // 如果当前连接存在，才打印断开提示
                if (activeLogStream != null) {
                    console.writeln(Ansi.error("\r\n[SYS] 日志连接已断开。"));
                    stopLogStream();
                }
            };

        } catch (Exception e) {
            console.writeln(Ansi.error("[SYS] 日志连接建立异常: " + e.getMessage()));
        }
    }

    /**
     * 规范化换行符，防止 Xterm 出现梯形错位
     */
    private String normalizeLineBreaks(String text) {
        if (text == null) return "";
        return text.replaceAll("\r?\n", "\r\n");
    }

    /**
     * 安全断开日志流长连接
     */
    private void stopLogStream() {
        closeTerminalWebSocket();
    }

    private void setControlToolsVisible(boolean visible) {
        btnStartService.setVisible(visible);
        btnExec.setVisible(visible);
        txtCommand.setVisible(visible);
    }

    @UiHandler("btnExec")
    public void btnExecClick(ClickEvent event) {
        if (StringUtil.isBlank(currentSelectedService)) return;

        String shell = txtCommand.getText();
        if (StringUtil.isBlank(shell)) {
            shell = "/bin/bash";
        }

        // 点击执行按钮时，断开之前的日志 SSE 流，切换为交互式 WebSocket Shell
        connectContainerShell(currentSelectedService, shell);
    }

    @UiHandler("btnRestart")
    public void btnRestartClick(ClickEvent event) {
        RestartDockerAppRequest request = new RestartDockerAppRequest();
        request.setDockerAppId(appEntity.getId());
        request.setAction(DockerAppAction.DAA_RESTART.getAction());
        executeAction(request);
    }

    @UiHandler("btnStop")
    public void btnStopClick(ClickEvent event) {
        RestartDockerAppRequest request = new RestartDockerAppRequest();
        request.setDockerAppId(appEntity.getId());
        request.setAction(DockerAppAction.DAA_SHUTDOWN.getAction());
        executeAction(request);
    }

    @UiHandler("btnStartService")
    public void btnStartServiceClick(ClickEvent event) {
        RestartDockerAppRequest request = new RestartDockerAppRequest();
        request.setDockerAppId(appEntity.getId());
        request.setAction(DockerAppAction.DAA_RESTART.getAction());
        request.setServiceName((String) btnStartService.getData());
        executeAction(request);
    }

    @UiHandler("lbTitle")
    public void lbTitleClick(ClickEvent event) {
        setData(appEntity);
    }

    private void executeAction(RestartDockerAppRequest request) {
        AppProxy.get().restartDockerApp(request, new AsyncCallback<RpcResult<RestartDockerAppResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().confirm(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<RestartDockerAppResponse> result) {
                if (result.isSuccess()) {
                    ClientContext.get().toast(0, 0, "操作成功");
                    loadLog(request.getServiceName());
                } else {
                    ClientContext.get().confirm(result.getMessage());
                }
            }
        });
    }

    private void loadLog(String serviceName) {
        connectServiceLogs(serviceName);
    }

    /**
     * 连接容器 Shell 终端
     */
    private void connectContainerShell(String serviceName, String shellCmd) {
        stopLogStream(); // 停止 SSE 日志流
        closeTerminalWebSocket(); // 彻底清理并断开旧的 WebSocket 及 Addon

        console.write("\033[2J\033[3J\033[H");
        console.writeln(Ansi.info("[SYS] 正在连接容器 [" + serviceName + "] 的 Shell 终端...\r\n"));

        String protocol = DomGlobal.window.location.protocol.startsWith("https") ? "wss://" : "ws://";
        String wsUrl = protocol + DomGlobal.window.location.host + "/ws/docker/exec/" + appEntity.getId() + "/" + serviceName;

        try {
            // 创建新的 WebSocket 对象
            final elemental2.dom.WebSocket ws = new elemental2.dom.WebSocket(wsUrl);
            terminalWebSocket = ws;

            ws.onopen = evt -> {
                // 确保只有当前最新的 WebSocket 才能挂载 Addon
                if (terminalWebSocket != ws) {
                    ws.close();
                }

                console.writeln(Ansi.success("[SYS] 容器 Shell 已连通！\r\n"));

                // 实例化并挂载 AttachAddon
                AttachOptions.Builder builder = new AttachOptions.Builder();
                builder.setBidirectional(true);

                currentAttach = new AttachAddon(ws, builder.build());
                console.loadAddon(currentAttach);

                // 延时重置终端大小并获取焦点
                Scheduler.get().scheduleDeferred(() -> {
                    fitTerminal();
                    console.focus();
                });
            };

            ws.onerror = evt -> {
                if (terminalWebSocket == ws) {
                    console.writeln(Ansi.error("\r\n[SYS] 容器 Shell 连接出现错误"));
                }
            };

            ws.onclose = evt -> {
                // 防误杀判断：只有当关闭的是“当前激活的” WebSocket 时才打印并清理
                if (terminalWebSocket == ws) {
                    console.writeln(Ansi.warn("\r\n[SYS] 容器 Shell 会话已关闭。"));
                    closeTerminalWebSocket();
                }
            };

        } catch (Exception e) {
            console.writeln(Ansi.error("[SYS] 建立 WebSocket 连接失败: " + e.getMessage()));
        }
    }

    /**
     * 彻底清理旧的 AttachAddon 和 WebSocket 实例
     */
    private void closeTerminalWebSocket() {
        // 1. 优先卸载并销毁 Addon（解绑 Xterm 监听）
        if (currentAttach != null) {
            try {
                currentAttach.dispose();
            } catch (Exception ignored) {
            }
            currentAttach = null;
        }

        // 2. 断开并注销 WebSocket
        if (terminalWebSocket != null) {
            elemental2.dom.WebSocket oldWs = terminalWebSocket;
            terminalWebSocket = null; // 先将全局指针置空

            // 解绑回调，防止异步 close 触发老句柄的 onclose 回调误杀新连接
            oldWs.onopen = null;
            oldWs.onmessage = null;
            oldWs.onerror = null;
            oldWs.onclose = null;

            try {
                if (oldWs.readyState != elemental2.dom.WebSocket.CLOSED) {
                    oldWs.close();
                }
            } catch (Exception ignored) {
            }
        }
        if (activeLogStream != null) {
            activeLogStream.close();
            activeLogStream = null;
        }
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        stopLogStream();
        closeTerminalWebSocket();
    }


    @Override
    protected void onLoad() {
        super.onLoad();
        fitTerminal();
    }

    @Override
    public void onResize() {
        root.onResize();
        fitTerminal();
    }

    /**
     * 强行让 Terminal 重新计算 Cols 和 Rows 以填满容器
     */
    private void fitTerminal() {
        if (terminal != null) {
            // 延迟一帧，确保 DOM 已经完全渲染并具有实际宽高像素
            Scheduler.get().scheduleDeferred(() -> {
                // 方法 A：如果你的 TerminalPanel 包装类有 fit() 方法
                terminal.resize();
            });
        }
    }

    public interface SStyle extends CssResource {
        String box();

        String sizeLabel();
    }

    interface DockerAppOperatorPanelUiBinder extends UiBinder<DockLayoutPanel, DockerAppOperatorPanel> {
    }

    // ANSI 终端控制序列常量 (使用 \033 避免 Java 编译错误)
    private static class Ansi {
        public static final String RESET = "\033[0m";
        public static final String RED = "\033[31m";
        public static final String GREEN = "\033[32m";
        public static final String YELLOW = "\033[33m";
        public static final String CYAN = "\033[36m";

        public static String warn(String msg) {
            return YELLOW + msg + RESET;
        }

        public static String error(String msg) {
            return RED + msg + RESET;
        }

        public static String info(String msg) {
            return CYAN + msg + RESET;
        }

        public static String success(String msg) {
            return GREEN + msg + RESET;
        }
    }
}