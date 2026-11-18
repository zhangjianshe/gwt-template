package cn.mapway.gwt_template.client.drive;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.widget.TextBox;
import cn.mapway.rbac.client.user.SearchUserPanel;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.frame.ToolbarModule;
import cn.mapway.ui.client.mvc.*;
import cn.mapway.ui.client.widget.FontIcon;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.dialog.Dialog;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.*;
import com.google.gwt.user.client.ui.*;
import elemental2.core.JsDate;
import elemental2.dom.*;
import java.util.*;

@ModuleMarker(
    value = DriveFrame.MODULE_CODE,
    name = "网盘",
    summary = "目录、文件和目录访问权限管理",
    unicode = Fonts.APPS,
    order = 210)
public class DriveFrame extends ToolbarModule {
  public static final String MODULE_CODE = "drive_frame";
  private static boolean styled;
  private final Tree tree = new Tree();
  private final Map<String, TreeItem> treeItems = new HashMap<>();
  private final Set<String> loaded = new HashSet<>();
  private final Map<String, Integer> branchRequests = new HashMap<>();
  private final FlowPanel crumbs = new FlowPanel();
  private final Label status = new Label();
  private final Label pageLabel = new Label();
  private final Label uploadLabel = new Label();
  private final FlexTable table = new FlexTable();
  private Node rootNode = new Node("root", "我的网盘", null);
  private final TabLayoutPanel modes = new TabLayoutPanel(36, Unit.PX);
  private String scope = "mine";
  private boolean ready, manageable;
  private int currentPermissions;
  private JSONObject entryPermissions = new JSONObject();
  private Node current = rootNode;
  private int page = 1, total, requestVersion, lifecycle;
  private String renderedDirectory, renderedJson;
  private int renderedPage;
  private final com.google.gwt.user.client.Timer directorySizeRefresh =
      new com.google.gwt.user.client.Timer() {
        public void run() {
          if (isAttached()) loadPage(page, true);
        }
      };
  private final Map<String, JSONObject> selectedEntries = new LinkedHashMap<>();
  private final Map<String, CheckBox> pageSelections = new LinkedHashMap<>();
  private CheckBox selectAll;
  private boolean deleting;
  private Runnable deleteProgressOpener;
  private Runnable stopDeleteProgress;
  private String deleteOwner = "";
  private final AiButton deleteSelected = button("删除", this::deleteSelected);
  private boolean transferring;
  private XMLHttpRequest transfer;

  private final AiButton create = button("新建目录", this::mkdir);
  private final AiButton upload = button("上传文件", this::chooseFiles);
  private final AiButton statistics = button("大小统计", this::showStatistics);
  private final AiButton shares = button("共享授权", this::manageShares);
  private final AiButton tokens = button("目录 token", this::manageTokens);
  private final AiButton retry = button("重试", () -> loadPage(page));
  private final AiButton previous = button("上一页", () -> loadPage(page - 1));
  private final AiButton next = button("下一页", () -> loadPage(page + 1));
  private final AiButton cancelUpload =
      button(
          "取消上传",
          () -> {
            if (transfer != null) transfer.abort();
          });
  private final HTMLInputElement fileInput =
      (HTMLInputElement) DomGlobal.document.createElement("input");

  private static class Node {
    final String id, name;
    final Node parent;

    Node(String id, String name, Node parent) {
      this.id = id;
      this.name = name;
      this.parent = parent;
    }
  }

  private static AiButton button(String text, Runnable action) {
    AiButton button = new AiButton();
    button.setText(text);
    button.addClickHandler(event -> action.run());
    return button;
  }

  private static Label label(String text, String style) {
    Label value = new Label(text);
    value.setStyleName(style);
    return value;
  }

  public DriveFrame() {
    injectStyles();
    DockLayoutPanel layout = new DockLayoutPanel(Unit.PX);
    layout.setStyleName("drive-page");
    DockLayoutPanel sidebar = new DockLayoutPanel(Unit.PX);
    sidebar.setStyleName("drive-sidebar");
    sidebar.add(new ScrollPanel(tree));
    layout.addWest(sidebar, 260);
    DockLayoutPanel content = new DockLayoutPanel(Unit.PX);
    FlowPanel heading = new FlowPanel();
    heading.setStyleName("drive-heading");
    crumbs.setStyleName("drive-crumbs");
    heading.add(crumbs);
    FlowPanel state = new FlowPanel();
    state.setStyleName("drive-state");
    status.getElement().getStyle().setProperty("userSelect", "text");
    state.add(status);
    state.add(retry);
    heading.add(state);
    content.addNorth(heading, 90);
    FlowPanel footer = new FlowPanel();
    footer.setStyleName("drive-footer");
    footer.add(pageLabel);
    footer.add(previous);
    footer.add(next);
    content.addSouth(footer, 58);
    FlowPanel uploadState = new FlowPanel();
    uploadState.setStyleName("drive-upload-state");
    uploadState.add(uploadLabel);
    uploadState.add(cancelUpload);
    footer.insert(uploadState, 0);
    table.setStyleName("drive-table");
    table.addStyleName("drive-list-table");
    table.setWidth("100%");
    content.add(new ScrollPanel(table));
    layout.add(content);
    modes.add(new SimpleLayoutPanel(), "我的网盘");
    modes.add(new SimpleLayoutPanel(), "共享给我");
    modes.addSelectionHandler(event -> {
      ((SimpleLayoutPanel) modes.getWidget(event.getSelectedItem())).setWidget(layout);
      if (ready) resetView();
    });
    modes.selectTab(0);
    ((SimpleLayoutPanel) modes.getWidget(0)).setWidget(layout);
    initWidget(modes);
    tree.addSelectionHandler(event -> {
      Object data = event.getSelectedItem().getUserObject();
      if (data instanceof Node && !((Node) data).id.equals(current.id))
        navigate((Node) data, false);
    });
    tree.addOpenHandler(event -> {
      TreeItem item = event.getTarget();
      if (item.getUserObject() instanceof Node
          && !loaded.contains(((Node) item.getUserObject()).id)) loadBranch(item);
    });
    fileInput.type = "file";
    fileInput.multiple = true;
    fileInput.onchange =
        event -> {
          if (transferring || fileInput.files == null || fileInput.files.getLength() == 0)
            return null;
          List<elemental2.dom.File> files = new ArrayList<>();
          for (int i = 0; i < fileInput.files.getLength(); i++) files.add(fileInput.files.getAt(i));
          fileInput.value = "";
          transferring = true;
          updateActions();
          cancelUpload.setVisible(true);
          uploadNext(files, 0, current);
          return null;
        };
    cancelUpload.setVisible(false);
    retry.setVisible(false);
  }

  @Override
  public String getModuleCode() {
    return MODULE_CODE;
  }

  @Override
  public boolean initialize(IModule parent, ModuleParameter parameter) {
    super.initialize(parent, parameter);
    resetDeleteAccount();
    renderedDirectory = null;
    renderedJson = null;
    lifecycle++;
    updateTools(
        create, upload, deleteSelected, statistics, shares, tokens, button("刷新", this::refresh));
    ready = false;
    if (ClientContext.get().isAdmin() && modes.getWidgetCount() == 2)
      modes.add(new SimpleLayoutPanel(), "全部网盘");
    if (!ClientContext.get().isAdmin() && modes.getWidgetCount() == 3) modes.remove(2);
    modes.selectTab(0);
    ready = true;
    resetView();
    return true;
  }

  private void resetView() {
    scope =
        modes.getSelectedIndex() == 1 ? "shared" : modes.getSelectedIndex() == 2 ? "all" : "mine";
    rootNode =
        new Node(
            "root", "shared".equals(scope) ? "共享给我" : "all".equals(scope) ? "全部网盘" : "我的网盘", null);
    current = rootNode;
    lifecycle++;
    requestVersion++;
    currentPermissions = 0;
    manageable = false;
    selectedEntries.clear();
    pageSelections.clear();
    entryPermissions = new JSONObject();
    renderedDirectory = null;
    renderedJson = null;
    table.removeAllRows();
    refreshTree();
    navigate(rootNode);
  }

  private void updateActions() {
    updateSelection();
    boolean root = "root".equals(current.id);
    create.setEnabled((currentPermissions & 2) != 0 && !(root && "shared".equals(scope)));
    upload.setEnabled((currentPermissions & 2) != 0 && !transferring && !root);
    statistics.setEnabled((currentPermissions & 1) != 0 && (!root || "all".equals(scope)));
    tokens.setEnabled(manageable);
    shares.setEnabled(manageable);
    upload.setTitle("root".equals(current.id) ? "请先选择或创建一个目录" : "上传文件到当前目录");
  }

  private boolean onCurrentPath(String id) {
    for (Node node = current; node != null; node = node.parent) if (node.id.equals(id)) return true;
    return false;
  }

  private TreeItem treeItem(Node node) {
    TreeItem item = new TreeItem(new Label(node.name));
    item.setUserObject(node);
    item.addItem(new TreeItem(new Label("展开加载…")));
    treeItems.put(node.id, item);
    return item;
  }

  private void refreshTree() {
    tree.clear();
    treeItems.clear();
    loaded.clear();
    branchRequests.clear();
    TreeItem root = treeItem(rootNode);
    tree.addItem(root);
    root.setState(true, false);
    loadBranch(root);
  }

  private void loadBranch(TreeItem item) {
    Node node = (Node) item.getUserObject();
    int generation = lifecycle;
    int version = branchRequests.getOrDefault(node.id, 0) + 1;
    branchRequests.put(node.id, version);
    boolean expanded = item.getState();
    item.removeItems();
    item.addItem(new TreeItem(new Label("加载目录…")));
    item.setState(expanded, false);
    loadBranchPage(item, node, 1, new ArrayList<>(), generation, version);
  }

  private void loadBranchPage(
      TreeItem item, Node node, int number, List<Node> children, int generation, int version) {
    DriveApi.request(
        "GET",
        "/entries?scope="
            + scope
            + "&parentId="
            + URL.encodeQueryString(node.id)
            + "&pageSize=1000&page="
            + number,
        null,
        new DriveApi.Callback() {
          private boolean valid() {
            return generation == lifecycle
                && treeItems.get(node.id) == item
                && branchRequests.get(node.id) == version;
          }

          public void success(JSONObject data) {
            if (!valid()) return;
            JSONArray entries = DriveApi.array(data, "entries");
            for (int i = 0; i < entries.size(); i++) {
              JSONObject entry = entries.get(i).isObject();
              if (DriveApi.directory(entry))
                children.add(
                    new Node(DriveApi.string(entry, "id"), DriveApi.string(entry, "name"), node));
            }
            if (number * 1000 < DriveApi.number(data, "total")) {
              loadBranchPage(item, node, number + 1, children, generation, version);
              return;
            }
            boolean expanded = item.getState();
            item.removeItems();
            loaded.add(node.id);
            for (Node child : children) {
              TreeItem value = treeItem(child);
              item.addItem(value);
              if (child.id.equals(current.id)) {
                tree.setSelectedItem(value, false);
                value.setState(true, false);
                loadBranch(value);
              } else if (onCurrentPath(child.id)) {
                value.setState(true, false);
                loadBranch(value);
              }
            }
            item.setState(expanded, false);
            if (node.id.equals(current.id)) tree.setSelectedItem(item, false);
          }

          public void failure(String message) {
            if (!valid()) return;
            boolean expanded = item.getState();
            item.removeItems();
            Anchor again = new Anchor("加载失败，点击重试");
            again.setTitle(message);
            again.addClickHandler(event -> loadBranch(item));
            item.addItem(new TreeItem(again));
            item.setState(expanded, false);
          }
        });
  }

  private void navigate(Node node) {
    navigate(node, true);
  }

  private void navigate(Node node, boolean expand) {
    current = node;
    currentPermissions = 0;
    manageable = false;
    updateActions();
    crumbs.clear();
    List<Node> path = new ArrayList<>();
    for (Node value = node; value != null; value = value.parent) path.add(value);
    Collections.reverse(path);
    for (Node value : path) {
      if (crumbs.getWidgetCount() > 0) crumbs.add(label("›", "drive-separator"));
      Anchor link = new Anchor(value.name);
      link.addClickHandler(event -> navigate(value));
      crumbs.add(link);
    }
    TreeItem item = treeItems.get(node.id);
    if (item != null) {
      tree.setSelectedItem(item, false);
      if (expand) {
        item.setState(true, false);
        if (!loaded.contains(node.id)) loadBranch(item);
      }
    }
    loadPage(1);
  }

  private void refresh() {
    refreshTree();
    loadPage(page);
  }

  @Override
  protected void onUnload() {
    if (stopDeleteProgress != null) stopDeleteProgress.run();
    directorySizeRefresh.cancel();
    requestVersion++;
    lifecycle++;
    super.onUnload();
  }

  private void loadPage(int wanted) {
    loadPage(wanted, false);
  }

  private void loadPage(int wanted, boolean background) {
    directorySizeRefresh.cancel();
    page = Math.max(1, wanted);
    int version = ++requestVersion;
    if (!background) {
      selectedEntries.clear();
      updateSelection();
      status.setStyleName("drive-muted");
      status.setText("正在加载文件…");
      retry.setVisible(false);
      previous.setEnabled(false);
      next.setEnabled(false);
      table.getElement().setAttribute("aria-busy", "true");
      table.getElement().setAttribute("inert", "");
    }
    DriveApi.request(
        "GET",
        "/entries?scope="
            + scope
            + "&parentId="
            + URL.encodeQueryString(current.id)
            + "&pageSize=100&page="
            + page,
        null,
        new DriveApi.Callback() {
          public void success(JSONObject data) {
            if (version != requestVersion) return;
            currentPermissions = (int) DriveApi.number(data, "permissions");
            manageable =
                data.get("manage") != null
                    && data.get("manage").isBoolean() != null
                    && data.get("manage").isBoolean().booleanValue();
            entryPermissions =
                data.get("entryPermissions") == null
                    ? new JSONObject()
                    : data.get("entryPermissions").isObject();
            for (String id : new ArrayList<>(selectedEntries.keySet()))
              if (!canDelete(id)) selectedEntries.remove(id);
            updateActions();
            total = (int) DriveApi.number(data, "total");
            if (page > 1 && (page - 1) * 100 >= total) {
              loadPage(Math.max(1, (total + 99) / 100), background);
              return;
            }
            status.setText(
                "共 "
                    + total
                    + " 项"
                    + ("root".equals(current.id) ? "" : " · 目录 ID：" + current.id)
                    + " · 共享权限对授权目录及其子目录生效");
            JSONValue saved = data.get("directoryStatistics");
            JSONObject sizes =
                saved != null && saved.isObject() != null ? saved.isObject() : new JSONObject();
            JSONArray entries = DriveApi.array(data, "entries");
            String signature = entries.toString();
            boolean pending;
            if (current.id.equals(renderedDirectory)
                && renderedPage == page
                && signature.equals(renderedJson)) {
              pending = updateDirectorySizes(entries, sizes);
              updateSelection();
            } else {
              table.removeAllRows();
              pending = render(entries, sizes);
              renderedDirectory = current.id;
              renderedPage = page;
              renderedJson = signature;
            }
            table.getElement().removeAttribute("inert");
            table.getElement().setAttribute("aria-busy", "false");
            if (pending) directorySizeRefresh.schedule(1000);
            pageLabel.setText("第 " + page + " / " + Math.max(1, (total + 99) / 100) + " 页");
            previous.setEnabled(page > 1);
            next.setEnabled(page * 100 < total);
          }

          public void failure(String message) {
            if (version != requestVersion) return;
            currentPermissions = 0;
            manageable = false;
            entryPermissions = new JSONObject();
            selectedEntries.clear();
            updateActions();
            status.setStyleName("drive-error");
            status.setText(background ? message : message + "；当前保留上次列表，请重试");
            retry.setVisible(true);
            pageLabel.setText("");
          }
        });
  }

  private boolean render(JSONArray entries, JSONObject directoryStatistics) {
    boolean pending = false;
    Set<String> visible = new HashSet<>();
    for (int i = 0; i < entries.size(); i++)
      visible.add(DriveApi.string(entries.get(i).isObject(), "id"));
    selectedEntries.keySet().retainAll(visible);
    pageSelections.clear();
    String[] headings = {"", "文件名称", "大小", "创建时间", "操作"};
    String[] widths = {"44px", "auto", "140px", "160px", "100px"};
    for (int i = 0; i < headings.length; i++) {
      table.setText(0, i, headings[i]);
      table.getCellFormatter().setWidth(0, i, widths[i]);
    }
    table.getRowFormatter().setStyleName(0, "drive-table-head");
    selectAll = new CheckBox();
    selectAll.setTitle("全选当前页");
    selectAll
        .getElement()
        .getElementsByTagName("input")
        .getItem(0)
        .setAttribute("aria-label", "全选当前页");
    selectAll.addValueChangeHandler(event -> {
      for (int i = 0; i < entries.size(); i++) {
        JSONObject entry = entries.get(i).isObject();
        String id = DriveApi.string(entry, "id");
        if (Boolean.TRUE.equals(event.getValue()) && canDelete(id)) selectedEntries.put(id, entry);
        else selectedEntries.remove(id);
      }
      updateSelection();
    });
    table.setWidget(0, 0, selectAll);
    table.getCellFormatter().addStyleName(0, 0, "drive-selection");
    table.getCellFormatter().addStyleName(0, 2, "drive-size");
    if (entries.size() == 0) {
      table.setText(1, 0, "root".equals(current.id) ? "暂无目录，请点击新建目录" : "目录为空，可新建目录或上传文件");
      table.getFlexCellFormatter().setColSpan(1, 0, 5);
      table.getRowFormatter().setStyleName(1, "drive-empty");
      updateSelection();
      return false;
    }
    for (int i = 0; i < entries.size(); i++) {
      JSONObject entry = entries.get(i).isObject();
      int row = i + 1;
      boolean dir = DriveApi.directory(entry);
      String id = DriveApi.string(entry, "id");
      CheckBox selected = new CheckBox();
      selected.setTitle("选择「" + DriveApi.string(entry, "name") + "」");
      selected
          .getElement()
          .getElementsByTagName("input")
          .getItem(0)
          .setAttribute("aria-label", "选择「" + DriveApi.string(entry, "name") + "」");
      selected.addValueChangeHandler(event -> {
        if (Boolean.TRUE.equals(event.getValue()) && canDelete(id)) selectedEntries.put(id, entry);
        else selectedEntries.remove(id);
        updateSelection();
      });
      pageSelections.put(id, selected);
      selected.setEnabled(canDelete(id));
      table.setWidget(row, 0, selected);
      table.getCellFormatter().addStyleName(row, 0, "drive-selection");
      String name = DriveApi.string(entry, "name");
      FlowPanel nameCell = new FlowPanel();
      nameCell.setStyleName("drive-entry-name");
      FontIcon<Void> icon = new FontIcon<>();
      icon.setIconUnicode(dir ? Fonts.FOLDER : Fonts.FILE);
      icon.addStyleName("drive-entry-icon");
      icon.addStyleName(dir ? "drive-entry-folder" : "drive-entry-file");
      icon.setTitle(dir ? "目录" : "文件");
      icon.getElement().setAttribute("role", "img");
      icon.getElement().setAttribute("aria-label", dir ? "目录" : "文件");
      nameCell.add(icon);
      if (dir) {
        Node child = new Node(DriveApi.string(entry, "id"), name, current);
        Anchor link = new Anchor(name);
        link.addClickHandler(event -> navigate(child));
        link.addStyleName("drive-entry-text");
        nameCell.add(link);
      } else {
        Label value = new Label(name);
        value.setTitle("SHA-256: " + DriveApi.string(entry, "sha256"));
        value.addStyleName("drive-entry-text");
        nameCell.add(value);
      }
      if ("root".equals(current.id) && !"mine".equals(scope)) {
        String owner =
            entry.get("ownerId") == null || entry.get("ownerId").isNull() != null
                ? "历史未归属"
                : "所有者 ID：" + entry.get("ownerId").toString();
        nameCell.add(label(owner, "drive-muted"));
      }
      table.setWidget(row, 1, nameCell);
      if (dir) pending |= updateDirectorySize(row, entry, directoryStatistics);
      else table.setText(row, 2, size(DriveApi.number(entry, "size")));
      table.getCellFormatter().addStyleName(row, 2, "drive-size");
      String created = DriveApi.string(entry, "createTime");
      Label createdTime = new Label(relativeTime(created));
      createdTime.setTitle(formatTime(created));
      table.setWidget(row, 3, createdTime);
      FlowPanel actions = new FlowPanel();
      actions.setStyleName("drive-row-actions");
      if (!dir) {
        Anchor download =
            new Anchor(
                "下载",
                DriveApi.base()
                    + "/files/"
                    + URL.encodeQueryString(DriveApi.string(entry, "id"))
                    + "/content");
        download.getElement().setAttribute("download", name);
        download.setStyleName("drive-download");
        actions.add(download);
      }
      table.setWidget(row, 4, actions);
    }
    updateSelection();
    return pending;
  }

  private boolean updateDirectorySize(int row, JSONObject entry, JSONObject directoryStatistics) {
    JSONValue saved = directoryStatistics.get(DriveApi.string(entry, "id"));
    JSONObject stats = saved != null && saved.isObject() != null ? saved.isObject() : null;
    JSONValue flag = stats == null ? null : stats.get("initialized");
    boolean initialized =
        flag != null && flag.isBoolean() != null && flag.isBoolean().booleanValue();
    Widget existing = table.getCellCount(row) > 2 ? table.getWidget(row, 2) : null;
    Label value = existing instanceof Label ? (Label) existing : new Label();
    String text = initialized ? size(DriveApi.number(stats, "totalSize")) : "统计中";
    if (!text.equals(value.getText())) value.setText(text);
    String state = DriveApi.string(stats, "state");
    value.setTitle(
        initialized
            ? "递归总大小：" + (long) DriveApi.number(stats, "totalSize") + " 字节"
            : "FAILED".equals(state) ? "统计失败，请在大小统计中重新统计" : "正在后台初始化目录大小");
    if (existing != value) table.setWidget(row, 2, value);
    return !initialized && !"FAILED".equals(state);
  }

  private boolean updateDirectorySizes(JSONArray entries, JSONObject sizes) {
    boolean pending = false;
    for (int i = 0; i < entries.size(); i++) {
      JSONObject entry = entries.get(i).isObject();
      if (DriveApi.directory(entry)) pending |= updateDirectorySize(i + 1, entry, sizes);
      Widget created = table.getWidget(i + 1, 3);
      if (created instanceof Label) {
        String text = relativeTime(DriveApi.string(entry, "createTime"));
        if (!text.equals(((Label) created).getText())) ((Label) created).setText(text);
      }
    }
    return pending;
  }

  private boolean canDelete(String id) {
    return entryPermissions != null && (((int) DriveApi.number(entryPermissions, id)) & 4) != 0;
  }

  private void updateSelection() {
    for (Map.Entry<String, CheckBox> item : pageSelections.entrySet()) {
      item.getValue().setValue(selectedEntries.containsKey(item.getKey()), false);
      item.getValue().setEnabled(!deleting && canDelete(item.getKey()));
    }
    if (selectAll != null) {
      int count = selectedEntries.size(), total = pageSelections.size();
      selectAll.setValue(total > 0 && count == total, false);
      selectAll.setEnabled(total > 0 && !deleting);
      com.google.gwt.dom.client.Element input =
          selectAll.getElement().getElementsByTagName("input").getItem(0);
      if (input != null) input.setPropertyBoolean("indeterminate", count > 0 && count < total);
    }
    deleteSelected.setEnabled(deleting || !selectedEntries.isEmpty());
    deleteSelected.setText(
        deleting
            ? "删除进度"
            : selectedEntries.isEmpty() ? "删除" : "删除（" + selectedEntries.size() + "）");
  }

  private static String relativeTime(String value) {
    try {
      double timestamp = new JsDate(value).getTime();
      if (Double.isNaN(timestamp)) return value;
      long seconds = Math.max(0, (System.currentTimeMillis() - (long) timestamp) / 1000);
      if (seconds < 60) return "刚刚";
      if (seconds < 3600) return seconds / 60 + "分钟前";
      if (seconds < 86400) return seconds / 3600 + "小时前";
      if (seconds < 2592000) return seconds / 86400 + "天前";
      if (seconds < 31536000) return seconds / 2592000 + "个月前";
      return seconds / 31536000 + "年前";
    } catch (RuntimeException ignored) {
      return value;
    }
  }

  private static String formatTime(String value) {
    try {
      double time = new JsDate(value).getTime();
      if (!Double.isNaN(time))
        return DateTimeFormat.getFormat("yyyy-MM-dd HH:mm").format(new Date((long) time));
    } catch (RuntimeException ignored) {
    }
    return value;
  }

  private static String size(double bytes) {
    String[] units = {"B", "KB", "MB", "GB", "TB"};
    int index = 0;
    while (bytes >= 1024 && index < 4) {
      bytes /= 1024;
      index++;
    }
    return (Math.round(bytes * 10) / 10.0) + " " + units[index];
  }

  private void showStatistics() {
    Node folder = current;
    FlowPanel body = form();
    body.add(label(path(folder), "drive-section"));
    Label result = label("正在读取目录统计…", "drive-muted");
    result.getElement().setAttribute("role", "status");
    Label state = label("", "drive-muted"), time = label("", "drive-muted");
    body.add(result);
    body.add(state);
    body.add(time);
    body.add(
        label(
            "递归累计逻辑大小，共享内容的多个文件引用分别计入；不含未完成上传，子目录数不含当前目录。文件操作会同步更新计数，重新统计用于后台校准。", "drive-muted"));
    DialogBox popup = dialog("目录大小统计", body);
    FlowPanel footer = actions();
    AiButton recalculate = new AiButton();
    recalculate.setText("重新统计");
    footer.add(recalculate);
    footer.add(button("关闭", popup::hide));
    body.add(footer);
    class StatisticsView {
      final com.google.gwt.user.client.Timer poll =
          new com.google.gwt.user.client.Timer() {
            public void run() {
              refresh(false);
            }
          };

      void refresh(boolean calculate) {
        poll.cancel();
        recalculate.setEnabled(false);
        DriveApi.request(
            calculate ? "POST" : "GET",
            "/directories/"
                + URL.encodeQueryString(folder.id)
                + "/statistics"
                + (calculate ? "/recalculate" : ""),
            null,
            new DriveApi.Callback() {
              public void success(JSONObject data) {
                if (!popup.isShowing()) return;
                JSONValue initialized = data.get("initialized");
                boolean ready =
                    initialized != null
                        && initialized.isBoolean() != null
                        && initialized.isBoolean().booleanValue();
                String status = DriveApi.string(data, "state");
                boolean running = "RUNNING".equals(status) || "PENDING".equals(status);
                double bytes = DriveApi.number(data, "totalSize");
                result.setText(
                    ready
                        ? "总大小："
                            + size(bytes)
                            + "（"
                            + (long) bytes
                            + " 字节） · 文件："
                            + (long) DriveApi.number(data, "fileCount")
                            + " · 子目录："
                            + (long) DriveApi.number(data, "directoryCount")
                        : "尚无完整统计结果");
                state.setStyleName("FAILED".equals(status) ? "drive-error" : "drive-muted");
                state.setText(
                    running
                        ? (ready ? "后台校准中，当前显示已保存结果…" : "首次统计正在后台进行…")
                        : "FAILED".equals(status) ? "后台统计失败，请点击重新统计重试" : "已保存；文件操作会实时更新统计");
                String updated = DriveApi.string(data, "updatedTime"),
                    calibrated = DriveApi.string(data, "calibratedTime");
                time.setText(
                    updated.isEmpty()
                        ? ""
                        : "更新时间："
                            + updated
                            + (calibrated.isEmpty() ? "" : " · 校准时间：" + calibrated));
                recalculate.setEnabled(!running);
                if (running) poll.schedule(1000);
                popup.center();
              }

              public void failure(String message) {
                if (!popup.isShowing()) return;
                state.setStyleName("drive-error");
                state.setText("统计请求失败：" + message);
                recalculate.setEnabled(true);
              }
            });
      }
    }
    StatisticsView view = new StatisticsView();
    recalculate.addClickHandler(event -> view.refresh(true));
    popup.addCloseHandler(event -> view.poll.cancel());
    popup.center();
    view.refresh(false);
  }

  private void styleDialog(DialogBox dialog) {
    dialog.addStyleName("drive-dialog");
    dialog.setGlassEnabled(true);
    dialog.setGlassStyleName("drive-dialog-glass");
  }

  private DialogBox dialog(String title, FlowPanel body) {
    DialogBox dialog = new DialogBox(false, true);
    dialog.setText(title);
    styleDialog(dialog);
    dialog.setWidget(body);
    return dialog;
  }

  private FlowPanel form() {
    FlowPanel panel = new FlowPanel();
    panel.setStyleName("drive-form");
    return panel;
  }

  private FlowPanel actions() {
    FlowPanel panel = new FlowPanel();
    panel.setStyleName("drive-dialog-actions");
    return panel;
  }

  private void mkdir() {
    Node parent = current;
    FlowPanel body = form();
    body.add(label("创建位置：" + path(parent), "drive-muted"));
    TextBox name = new TextBox();
    name.setWidth("100%");
    name.getElement().setAttribute("placeholder", "输入目录名称");
    name.getElement().setAttribute("aria-label", "目录名称");
    body.add(name);
    Label error = label("", "drive-error");
    body.add(error);
    DialogBox popup = dialog("新建目录", body);
    FlowPanel footer = actions();
    AiButton cancel = button("取消", popup::hide);
    AiButton save = new AiButton();
    save.setText("创建");
    save.addClickHandler(event -> {
      String value = name.getValue();
      if (value.trim().isEmpty()) {
        error.setText("请输入目录名称");
        return;
      }
      save.setEnabled(false);
      cancel.setEnabled(false);
      JSONObject request = DriveApi.object("parentId", parent.id);
      request.put("name", new JSONString(value));
      DriveApi.request(
          "POST",
          "/directories",
          request,
          new DriveApi.Callback() {
            public void success(JSONObject data) {
              popup.hide();
              refreshTree();
              if (current.id.equals(parent.id)) loadPage(1);
            }

            public void failure(String message) {
              error.setText(message);
              save.setEnabled(true);
              cancel.setEnabled(true);
            }
          });
    });
    footer.add(cancel);
    footer.add(save);
    body.add(footer);
    popup.center();
    Scheduler.get().scheduleDeferred(() -> name.setFocus(true));
  }

  private String accountKey() {
    return ClientContext.get().getUserInfo() == null
        ? ""
        : ClientContext.get().getUserInfo().getId();
  }

  private void resetDeleteAccount() {
    String account = accountKey();
    if (!deleteOwner.equals(account)) {
      if (stopDeleteProgress != null) stopDeleteProgress.run();
      deleteOwner = account;
      deleting = false;
      deleteProgressOpener = null;
      stopDeleteProgress = null;
      selectedEntries.clear();
    }
  }

  private void deleteSelected() {
    resetDeleteAccount();
    if (deleting) {
      if (deleteProgressOpener != null) deleteProgressOpener.run();
      return;
    }
    if (selectedEntries.isEmpty()) return;
    Node parent = current;
    String owner = accountKey();
    List<JSONObject> chosen = new ArrayList<>(selectedEntries.values());
    FlowPanel body = form();
    body.add(label("已选择 " + chosen.size() + " 项，确认批量删除？删除后不可恢复。", "drive-muted"));
    FlowPanel names = new FlowPanel();
    names.setStyleName("drive-delete-names");
    boolean directories = false;
    for (JSONObject entry : chosen) {
      names.add(
          new Label((DriveApi.directory(entry) ? "目录：" : "文件：") + DriveApi.string(entry, "name")));
      directories |= DriveApi.directory(entry);
    }
    body.add(names);
    CheckBox recursive = new CheckBox("递归删除所选目录中的全部子目录、文件和目录 token（不可恢复）");
    if (directories) body.add(recursive);
    Label progress = label("", "drive-muted"),
        connection = label("", "drive-muted"),
        error = label("", "drive-error");
    body.add(progress);
    body.add(connection);
    body.add(error);
    DialogBox popup = dialog("批量删除确认", body);
    FlowPanel footer = actions();
    AiButton close = button("取消", popup::hide);
    AiButton confirm = new AiButton();
    confirm.setText("确认删除 " + chosen.size() + " 项");
    footer.add(close);
    footer.add(confirm);
    body.add(footer);
    class DeleteView {
      String taskId = "", watchToken = "";
      long version = -1;
      WebSocket socket;
      boolean active;
      final com.google.gwt.user.client.Timer poll =
          new com.google.gwt.user.client.Timer() {
            public void run() {
              fetch();
            }
          };

      void disconnect() {
        poll.cancel();
        if (socket != null) {
          WebSocket old = socket;
          socket = null;
          old.onclose = null;
          old.onmessage = null;
          old.onerror = null;
          old.onopen = null;
          old.close();
        }
      }

      void connect() {
        if (!owner.equals(accountKey())) {
          disconnect();
          popup.hide();
          resetDeleteAccount();
          return;
        }
        if (taskId.isEmpty() || !popup.isShowing()) return;
        disconnect();
        active = true;
        try {
          String url = GWT.getHostPageBaseURL() + "ws/drive-delete";
          url = url.startsWith("https:") ? "wss:" + url.substring(6) : "ws:" + url.substring(5);
          socket = new WebSocket(url);
          WebSocket currentSocket = socket;
          socket.onopen =
              event -> {
                JSONObject subscription = DriveApi.object("taskId", taskId);
                subscription.put("watchToken", new JSONString(watchToken));
                currentSocket.send(subscription.toString());
                connection.setText("WebSocket 实时进度已连接");
              };
          socket.onmessage =
              event -> {
                try {
                  JSONObject data = JSONParser.parseStrict(event.data.asString()).isObject();
                  if (data != null) render(data);
                } catch (RuntimeException ignored) {
                }
              };
          socket.onclose =
              event -> {
                if (socket == currentSocket) {
                  socket = null;
                  connection.setText("实时连接已断开，正在查询任务进度…");
                }
              };
          socket.onerror =
              event -> {
                connection.setText("实时连接暂不可用，正在查询任务进度…");
              };
        } catch (RuntimeException failure) {
          connection.setText("正在通过任务状态查询进度…");
        }
        fetch();
      }

      void fetch() {
        if (!owner.equals(accountKey())) {
          disconnect();
          popup.hide();
          resetDeleteAccount();
          return;
        }
        if (!popup.isShowing() || taskId.isEmpty() || !active) return;
        DriveApi.request(
            "GET",
            "/delete-tasks/" + URL.encodeQueryString(taskId),
            null,
            new DriveApi.Callback() {
              public void success(JSONObject data) {
                if (!popup.isShowing()) return;
                render(data);
                if (active) poll.schedule(2000);
              }

              public void failure(String message) {
                if (!popup.isShowing()) return;
                connection.setText("查询失败：" + message + "；将继续重试");
                if (active) poll.schedule(3000);
              }
            });
      }

      void render(JSONObject data) {
        if (!owner.equals(accountKey())) {
          disconnect();
          popup.hide();
          resetDeleteAccount();
          return;
        }
        long next = (long) DriveApi.number(data, "version");
        if (next <= version) return;
        version = next;
        String state = DriveApi.string(data, "state"), phase = DriveApi.string(data, "phase");
        active = "RUNNING".equals(state) || "QUEUED".equals(state);
        String stage =
            "SCANNING".equals(phase)
                ? "扫描目录"
                : "DELETING".equals(phase)
                    ? "处理文件记录"
                    : "CLEANUP".equals(phase) ? "清理内容文件" : "QUEUED".equals(phase) ? "排队中" : "";
        progress.setText(
            "已处理 "
                + (long) DriveApi.number(data, "processed")
                + " / "
                + (long) DriveApi.number(data, "total")
                + " 项，成功 "
                + (long) DriveApi.number(data, "succeeded")
                + "，失败 "
                + (long) DriveApi.number(data, "failed")
                + (active
                    ? " · "
                        + DriveApi.string(data, "currentName")
                        + " "
                        + stage
                        + "（"
                        + (long) DriveApi.number(data, "entryProcessed")
                        + " / "
                        + (long) DriveApi.number(data, "entryTotal")
                        + "）"
                    : ""));
        JSONArray items = DriveApi.array(data, "items");
        StringBuilder failures = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
          JSONObject item = items.get(i).isObject();
          if ("SUCCESS".equals(DriveApi.string(item, "state")))
            selectedEntries.remove(DriveApi.string(item, "id"));
          else if ("FAILED".equals(DriveApi.string(item, "state")))
            failures
                .append(DriveApi.string(item, "name"))
                .append("：")
                .append(DriveApi.string(item, "error"))
                .append("\n");
        }
        if (!active) {
          disconnect();
          deleting = false;
          deleteProgressOpener = null;
          updateSelection();
          close.setText("关闭");
          connection.setText(
              "SUCCESS".equals(state)
                  ? "批量删除已完成"
                  : "INTERRUPTED".equals(state)
                      ? "任务因服务中断而停止，请刷新目录核对未处理项"
                      : "部分条目未删除，仍保留选择；可调整选项后重新提交");
          error.setText(failures.toString());
          boolean deletedCurrent = false;
          for (int i = 0; i < items.size(); i++) {
            JSONObject item = items.get(i).isObject();
            if ("SUCCESS".equals(DriveApi.string(item, "state"))
                && onCurrentPath(DriveApi.string(item, "id"))) deletedCurrent = true;
          }
          if (deletedCurrent) navigate(parent);
          else if (current.id.equals(parent.id)) loadPage(page, true);
          refreshTree();
        }
        updateSelection();
      }
    }
    DeleteView view = new DeleteView();
    stopDeleteProgress = view::disconnect;
    popup.addCloseHandler(event -> view.disconnect());
    confirm.addClickHandler(event -> {
      confirm.setEnabled(false);
      close.setEnabled(false);
      recursive.setEnabled(false);
      deleting = true;
      updateSelection();
      error.setText("");
      JSONArray ids = new JSONArray();
      for (int i = 0; i < chosen.size(); i++)
        ids.set(i, new JSONString(DriveApi.string(chosen.get(i), "id")));
      JSONObject request = new JSONObject();
      request.put("ids", ids);
      request.put("recursive", JSONBoolean.getInstance(Boolean.TRUE.equals(recursive.getValue())));
      DriveApi.request(
          "POST",
          "/delete-tasks",
          request,
          new DriveApi.Callback() {
            public void success(JSONObject data) {
              view.taskId = DriveApi.string(data, "id");
              view.watchToken = DriveApi.string(data, "watchToken");
              confirm.setVisible(false);
              close.setEnabled(true);
              close.setText("关闭窗口（任务继续）");
              recursive.setEnabled(false);
              popup.setText("批量删除进度");
              deleteProgressOpener =
                  () -> {
                    popup.center();
                    view.connect();
                  };
              view.render(data);
              if (view.active) view.connect();
            }

            public void failure(String message) {
              deleting = false;
              updateSelection();
              error.setText(message);
              confirm.setEnabled(true);
              close.setEnabled(true);
              recursive.setEnabled(true);
            }
          });
    });
    popup.center();
  }

  private String path(Node node) {
    if (node.parent == null) return "/";
    String parent = path(node.parent);
    return parent + (parent.endsWith("/") ? "" : "/") + node.name;
  }

  private void chooseFiles() {
    if (!transferring && !"root".equals(current.id)) fileInput.click();
  }

  private void uploadNext(List<elemental2.dom.File> files, int index, Node target) {
    if (index >= files.size()) {
      finishUpload(target, "已上传 " + files.size() + " 个文件到 " + path(target));
      return;
    }
    elemental2.dom.File file = files.get(index);
    String prefix =
        "上传到 " + path(target) + "（" + (index + 1) + "/" + files.size() + "）：" + file.name;
    uploadLabel.setText(prefix + " · 0%");
    transfer =
        DriveApi.upload(
            "/files?parentId="
                + URL.encodeQueryString(target.id)
                + "&name="
                + URL.encodeQueryString(file.name),
            file,
            percent -> uploadLabel.setText(prefix + " · " + percent + "%"),
            new DriveApi.Callback() {
              public void success(JSONObject data) {
                uploadNext(files, index + 1, target);
              }

              public void failure(String message) {
                finishUpload(target, file.name + "：" + message + "；其余未上传文件可重新选择。");
              }
            });
  }

  private void finishUpload(Node target, String message) {
    transfer = null;
    transferring = false;
    cancelUpload.setVisible(false);
    updateActions();
    uploadLabel.setText(message);
    if (current.id.equals(target.id)) loadPage(1);
  }

  private void manageShares() {
    if (!manageable) return;
    new ShareDialog(current).showCentered();
  }

  private class ShareDialog extends DialogBox {
    final Node folder;
    final FlowPanel body = form();
    final FlexTable grants = new FlexTable();
    final Label selectedUser = label("尚未选择用户", "drive-muted");
    String selectedUserId = "";
    Dialog<SearchUserPanel> picker;
    final AiButton grantSelected = button("保存授权", this::saveSelected);
    final CheckBox read = new CheckBox("读"), write = new CheckBox("写"), remove = new CheckBox("删除");
    final Label message = label("", "drive-error");
    int generation;

    ShareDialog(Node folder) {
      super(false, true);
      this.folder = folder;
      setText("共享目录给用户");
      styleDialog(this);
      addStyleName("drive-share-dialog");
      setWidget(body);
      body.add(label(path(folder), "drive-section"));
      body.add(label("授权对本目录及全部子目录生效；只有所有者或管理员可以管理授权。", "drive-muted"));
      grants.setStyleName("drive-table");
      grants.setWidth("100%");
      ScrollPanel list = new ScrollPanel(grants);
      list.setStyleName("drive-share-grants");
      body.add(list);
      FlowPanel userRow = new FlowPanel();
      userRow.setStyleName("drive-share-user");
      selectedUser.getElement().setAttribute("aria-live", "polite");
      userRow.add(selectedUser);
      userRow.add(button("选择用户", this::chooseUser));
      body.add(userRow);
      read.setValue(true);
      FlowPanel rights = new FlowPanel();
      rights.setStyleName("drive-permissions");
      rights.add(read);
      rights.add(write);
      rights.add(remove);
      rights.addStyleName("drive-share-new-permissions");
      grantSelected.setEnabled(false);
      rights.add(grantSelected);
      body.add(rights);
      body.add(message);
      FlowPanel footer = actions();
      footer.add(button("关闭", this::hide));
      body.add(footer);
    }

    void saveSelected() {
      if (selectedUserId.isEmpty()) {
        message.setText("请先选择用户");
        return;
      }
      save(selectedUserId, read, write, remove);
    }

    void chooseUser() {
      if (picker != null && picker.isShowing()) return;
      final Dialog<SearchUserPanel> dialog = SearchUserPanel.getDialog(false);
      picker = dialog;
      dialog.addStyleName("drive-share-user-picker");
      dialog.getElement().getStyle().setZIndex(5200);
      dialog.setGlassStyleName("drive-share-user-picker-glass");
      dialog.setPixelSize(
          Math.max(240, Math.min(520, com.google.gwt.user.client.Window.getClientWidth() - 48)),
          Math.max(240, Math.min(560, com.google.gwt.user.client.Window.getClientHeight() - 64)));
      dialog.addCommonHandler(event -> {
        if (event.isOk() && isShowing()) {
          RbacUserEntity user = event.getValue();
          if (user != null && user.getUserId() != null) {
            selectedUserId = user.getUserId().toString();
            selectedUser.setText(user.getUserName() + "（" + selectedUserId + "）");
            grantSelected.setEnabled(true);
            message.setText("");
          }
        }
        dialog.hide();
        if (picker == dialog) picker = null;
      });
      dialog.getContent().search("");
      dialog.center();
    }

    void showCentered() {
      center();
      load();
    }

    @Override
    public void hide() {
      generation++;
      if (picker != null) {
        picker.hide();
        picker = null;
      }
      super.hide();
    }

    void save(String userId, CheckBox r, CheckBox w, CheckBox d) {
      if (!selected(r, w, d)) {
        message.setText("至少选择一项权限；移除权限请使用撤销");
        return;
      }
      mutate("PUT", userId, permissions(r, w, d));
    }

    void mutate(String method, String userId, JSONObject request) {
      final int version = generation;
      DriveApi.request(
          method,
          "/directories/" + folder.id + "/shares/" + URL.encodeQueryString(userId),
          request,
          new DriveApi.Callback() {
            public void success(JSONObject data) {
              if (isShowing() && version == generation) {
                message.setText("授权已更新");
                load();
              }
            }

            public void failure(String error) {
              if (isShowing() && version == generation) message.setText(error);
            }
          });
    }

    void load() {
      final int version = ++generation;
      DriveApi.request(
          "GET",
          "/directories/" + folder.id + "/shares",
          null,
          new DriveApi.Callback() {
            public void success(JSONObject data) {
              if (!isShowing() || version != generation) return;
              grants.removeAllRows();
              JSONArray values = DriveApi.array(data, "shares");
              grants.setText(0, 0, "用户");
              grants.setText(0, 1, "读 / 写 / 删除");
              grants.setText(0, 2, "操作");
              grants.getRowFormatter().setStyleName(0, "drive-table-head");
              grants.getCellFormatter().setWidth(0, 0, "34%");
              grants.getCellFormatter().setWidth(0, 1, "32%");
              grants.getCellFormatter().setWidth(0, 2, "34%");
              if (values.size() == 0) grants.setText(1, 0, "尚未共享给其他用户");
              for (int i = 0; i < values.size(); i++) {
                JSONObject grant = values.get(i).isObject();
                final String userId = DriveApi.string(grant, "userId");
                int mask = (int) DriveApi.number(grant, "permissions");
                grants.setText(i + 1, 0, DriveApi.string(grant, "userName") + " (" + userId + ")");
                CheckBox r = new CheckBox("读"), w = new CheckBox("写"), d = new CheckBox("删除");
                r.setValue((mask & 1) != 0);
                w.setValue((mask & 2) != 0);
                d.setValue((mask & 4) != 0);
                FlowPanel rights = new FlowPanel();
                rights.setStyleName("drive-permissions");
                rights.add(r);
                rights.add(w);
                rights.add(d);
                grants.setWidget(i + 1, 1, rights);
                FlowPanel actions = new FlowPanel();
                actions.setStyleName("drive-row-actions");
                actions.add(button("保存", () -> save(userId, r, w, d)));
                actions.add(button("撤销", () -> mutate("DELETE", userId, null)));
                grants.setWidget(i + 1, 2, actions);
              }
            }

            public void failure(String error) {
              if (isShowing() && version == generation) message.setText(error);
            }
          });
    }
  }

  private void manageTokens() {
    if (!"root".equals(current.id)) new TokenDialog(current).showCentered();
  }

  private class TokenDialog extends DialogBox {
    final Node folder;
    final FlowPanel body = form(), secretPanel = new FlowPanel();
    final FlexTable grants = new FlexTable();
    final TextBox name = new TextBox();
    final CheckBox read = new CheckBox("读"), write = new CheckBox("写"), remove = new CheckBox("删除");
    final Label message = label("", "drive-error");
    final TextArea secret = new TextArea();
    final AiButton close = button("关闭", this::hide);
    int generation;

    TokenDialog(Node folder) {
      super(false, true);
      this.folder = folder;
      setText("目录 token 管理");
      styleDialog(this);
      addStyleName("drive-token-dialog");
      setWidget(body);
      body.add(label(path(folder), "drive-section"));
      body.add(label("目录 ID（用于命令行同步）", "drive-muted"));
      TextBox directoryId = new TextBox();
      directoryId.setReadOnly(true);
      directoryId.setValue(folder.id);
      directoryId.setWidth("100%");
      directoryId.getElement().setAttribute("aria-label", "同步目标目录ID");
      directoryId.addClickHandler(event -> directoryId.selectAll());
      body.add(directoryId);
      body.add(label("每个 token 对本目录及全部子目录生效。读、写、删除权限互不隐含。", "drive-muted"));
      grants.setStyleName("drive-table");
      grants.setWidth("100%");
      ScrollPanel scroll = new ScrollPanel(grants);
      scroll.setHeight("240px");
      body.add(scroll);
      body.add(label("创建 token", "drive-section"));
      name.setWidth("100%");
      name.setMaxLength(100);
      name.getElement().setAttribute("placeholder", "用途名称，例如：自动备份客户端");
      name.getElement().setAttribute("aria-label", "token名称");
      body.add(name);
      read.setValue(true);
      FlowPanel permissions = new FlowPanel();
      permissions.setStyleName("drive-permissions");
      permissions.add(read);
      permissions.add(write);
      permissions.add(remove);
      AiButton createToken = new AiButton();
      createToken.setText("创建 token");
      permissions.add(createToken);
      body.add(permissions);
      secretPanel.setStyleName("drive-secret");
      secretPanel.add(label("请立即复制保存：token 原文仅在创建时返回一次。", "drive-muted"));
      secret.setReadOnly(true);
      secret.setVisibleLines(2);
      secret.setWidth("100%");
      secret.getElement().setAttribute("aria-label", "新建的token原文");
      secretPanel.add(secret);
      AiButton copy =
          button(
              "复制 token",
              () -> {
                secret.setFocus(true);
                secret.selectAll();
                copyText(
                    secret.getValue(),
                    () -> message.setText("token 已复制，请安全保存"),
                    () -> message.setText("请按 Ctrl+C 或 Command+C 复制选中的 token"));
              });
      secretPanel.add(copy);
      body.add(secretPanel);
      secretPanel.setVisible(false);
      body.add(message);
      createToken.addClickHandler(event -> {
        if (name.getValue().trim().isEmpty()) {
          message.setText("请输入 token 名称");
          return;
        }
        if (!selected(read, write, remove)) {
          message.setText("至少选择一项权限");
          return;
        }
        createToken.setEnabled(false);
        close.setEnabled(false);
        JSONObject request = permissions(read, write, remove);
        request.put("name", new JSONString(name.getValue().trim()));
        DriveApi.request(
            "POST",
            "/directories/" + folder.id + "/tokens",
            request,
            new DriveApi.Callback() {
              public void success(JSONObject data) {
                createToken.setEnabled(true);
                close.setEnabled(true);
                if (!isShowing()) return;
                secret.setValue(DriveApi.string(data, "token"));
                secretPanel.setVisible(true);
                name.setValue("");
                message.setText("");
                loadGrants();
              }

              public void failure(String error) {
                createToken.setEnabled(true);
                close.setEnabled(true);
                message.setText(error);
              }
            });
      });
      FlowPanel footer = actions();
      footer.add(close);
      body.add(footer);
    }

    void showCentered() {
      center();
      loadGrants();
    }

    @Override
    public void hide() {
      generation++;
      secret.setValue("");
      secretPanel.setVisible(false);
      super.hide();
    }

    void loadGrants() {
      int version = ++generation;
      grants.removeAllRows();
      grants.setText(0, 0, "正在加载 token…");
      DriveApi.request(
          "GET",
          "/directories/" + folder.id + "/tokens",
          null,
          new DriveApi.Callback() {
            public void success(JSONObject data) {
              if (version != generation || !isShowing()) return;
              renderGrants(DriveApi.array(data, "tokens"));
            }

            public void failure(String error) {
              if (version != generation || !isShowing()) return;
              grants.removeAllRows();
              AiButton retry = button("重新加载 token", () -> loadGrants());
              grants.setWidget(0, 0, retry);
              message.setText(error);
            }
          });
    }

    void renderGrants(JSONArray list) {
      grants.removeAllRows();
      String[] headings = {"名称", "读", "写", "删除", "创建时间", "操作"};
      String[] widths = {"24%", "6%", "6%", "8%", "26%", "30%"};
      for (int i = 0; i < headings.length; i++) {
        grants.setText(0, i, headings[i]);
        grants.getCellFormatter().setWidth(0, i, widths[i]);
      }
      grants.getRowFormatter().setStyleName(0, "drive-table-head");
      if (list.size() == 0) {
        grants.setText(1, 0, "暂无 token，可在下方创建");
        grants.getFlexCellFormatter().setColSpan(1, 0, 6);
        return;
      }
      for (int i = 0; i < list.size(); i++) {
        JSONObject grant = list.get(i).isObject();
        int row = i + 1;
        String id = DriveApi.string(grant, "id"), title = DriveApi.string(grant, "name");
        int mask = (int) DriveApi.number(grant, "permissions");
        CheckBox r = new CheckBox(), w = new CheckBox(), d = new CheckBox();
        r.setValue((mask & 1) != 0);
        w.setValue((mask & 2) != 0);
        d.setValue((mask & 4) != 0);
        r.getElement().getFirstChildElement().setAttribute("aria-label", title + "：读取");
        w.getElement().getFirstChildElement().setAttribute("aria-label", title + "：写入");
        d.getElement().getFirstChildElement().setAttribute("aria-label", title + "：删除");
        grants.setWidget(row, 0, new Label(title));
        grants.setWidget(row, 1, r);
        grants.setWidget(row, 2, w);
        grants.setWidget(row, 3, d);
        grants.setText(row, 4, formatTime(DriveApi.string(grant, "createTime")));
        FlowPanel operations = new FlowPanel();
        operations.setStyleName("drive-row-actions");
        AiButton save = new AiButton();
        save.setText("保存权限");
        save.addClickHandler(event -> {
          if (!selected(r, w, d)) {
            message.setText("至少保留一项权限；停用请撤销 token");
            return;
          }
          save.setEnabled(false);
          close.setEnabled(false);
          DriveApi.request(
              "PATCH",
              "/directories/" + folder.id + "/tokens/" + id,
              permissions(r, w, d),
              new DriveApi.Callback() {
                public void success(JSONObject data) {
                  close.setEnabled(true);
                  message.setText("权限已更新");
                  loadGrants();
                }

                public void failure(String error) {
                  close.setEnabled(true);
                  save.setEnabled(true);
                  message.setText(error);
                }
              });
        });
        operations.add(save);
        operations.add(
            button(
                "撤销",
                () ->
                    ClientContext.get()
                        .confirmDelete("撤销 token「" + title + "」后，使用它的新请求将立即被拒绝。")
                        .then(ignored -> {
                          close.setEnabled(false);
                          DriveApi.request(
                              "DELETE",
                              "/directories/" + folder.id + "/tokens/" + id,
                              null,
                              new DriveApi.Callback() {
                                public void success(JSONObject data) {
                                  close.setEnabled(true);
                                  message.setText("token 已撤销");
                                  loadGrants();
                                }

                                public void failure(String error) {
                                  close.setEnabled(true);
                                  message.setText(error);
                                }
                              });
                          return null;
                        })));
        grants.setWidget(row, 5, operations);
      }
    }
  }

  private static boolean selected(CheckBox r, CheckBox w, CheckBox d) {
    return Boolean.TRUE.equals(r.getValue())
        || Boolean.TRUE.equals(w.getValue())
        || Boolean.TRUE.equals(d.getValue());
  }

  private static JSONObject permissions(CheckBox r, CheckBox w, CheckBox d) {
    JSONObject result = new JSONObject();
    result.put("read", JSONBoolean.getInstance(Boolean.TRUE.equals(r.getValue())));
    result.put("write", JSONBoolean.getInstance(Boolean.TRUE.equals(w.getValue())));
    result.put("delete", JSONBoolean.getInstance(Boolean.TRUE.equals(d.getValue())));
    return result;
  }

  private static native void copyText(String value, Runnable success, Runnable failure) /*-{
        if ($wnd.navigator.clipboard && $wnd.navigator.clipboard.writeText) {
            $wnd.navigator.clipboard.writeText(value).then($entry(function(){success.@java.lang.Runnable::run()();}),$entry(function(){failure.@java.lang.Runnable::run()();}));
        } else {
            try { if ($doc.execCommand('copy')) { success.@java.lang.Runnable::run()(); return; } } catch(e) {}
            failure.@java.lang.Runnable::run()();
        }
    }-*/;

  private static void injectStyles() {
    if (styled) return;
    styled = true;
    StyleInjector.inject(DriveResources.INSTANCE.styles().getText());
  }
}
