package cn.mapway.gwt_template.client.repository.basic;

import cn.mapway.ace.client.AceCommandDescription;
import cn.mapway.ace.client.AceEditor;
import cn.mapway.ace.client.AceEditorMode;
import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.widget.Head;
import cn.mapway.gwt_template.client.widget.IconButton;
import cn.mapway.gwt_template.shared.db.VwRepositoryEntity;
import cn.mapway.gwt_template.shared.rpc.dev.QueryRepositoryRequest;
import cn.mapway.gwt_template.shared.rpc.dev.QueryRepositoryResponse;
import cn.mapway.gwt_template.shared.rpc.repository.*;
import cn.mapway.gwt_template.shared.rpc.repository.git.GitRef;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.AiLabel;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepositoryCodeFrame extends CommonEventComposite implements IData<VwRepositoryEntity> {
    private static final ProjectCodeFrameUiBinder ourUiBinder = GWT.create(ProjectCodeFrameUiBinder.class);
    @UiField
    HorizontalPanel paths;
    @UiField
    HTMLPanel files;
    @UiField
    SStyle style;
    @UiField
    AceEditor editor;
    @UiField
    LayoutPanel contentPanel;
    @UiField
    ReferenceDropdown ddlRef;
    @UiField
    IconButton btnBranch;
    @UiField
    IconButton btnTag;
    @UiField
    CloneButton btnClone;
    @UiField
    Label dirLastUser;
    @UiField
    Label dirLastSummary;
    @UiField
    Label dirLastModify;
    @UiField
    HTMLPanel infoBar;
    @UiField
    HTMLPanel opBar;
    @UiField
    DockLayoutPanel root;
    @UiField
    ScrollPanel messagePanel;
    @UiField
    HTMLPanel msgContainer;
    @UiField
    ScrollPanel fileContainer;
    @UiField
    HTMLPanel pathBar;
    boolean initialize = false;
    Map<String, AceEditorMode> format = new HashMap<String, AceEditorMode>();
    ImportRepoPanel importRepoPanel;
    ImportingPanel importPanel;
    private VwRepositoryEntity project;

    public RepositoryCodeFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
        format.put("py", AceEditorMode.PYTHON);
        format.put("java", AceEditorMode.JAVA);
        format.put("js", AceEditorMode.JAVASCRIPT);
        format.put("jsm", AceEditorMode.JAVASCRIPT);
        format.put("md", AceEditorMode.MARKDOWN);
        format.put("c", AceEditorMode.C_CPP);
        format.put("json", AceEditorMode.JSON);
        format.put("yaml", AceEditorMode.YAML);
        format.put("yml", AceEditorMode.YAML);
        format.put("sh", AceEditorMode.SH);
        format.put("xml", AceEditorMode.XML);
        format.put("php", AceEditorMode.PHP);
        format.put("css", AceEditorMode.CSS);
        format.put("csharp", AceEditorMode.CSHARP);
        format.put("geojson", AceEditorMode.JSON);
        format.put("proto", AceEditorMode.PROTOBUF); // If your Ace version supports it
        format.put("sql", AceEditorMode.SQL);

        btnBranch.setValue(Fonts.BRANCH, "分支");
        btnTag.setValue(Fonts.LABEL, "标签");

        ddlRef.addValueChangeHandler(event -> {
            String ref = event.getValue();
            loadDir(ref, "");
        });
    }

    @Override
    public VwRepositoryEntity getData() {
        return project;
    }

    @Override
    public void setData(VwRepositoryEntity obj) {
        project = obj;
        toUI();
    }

    void reload() {
        if (project == null) {
            return;
        }
        QueryRepositoryRequest request = new QueryRepositoryRequest();
        request.setRepositoryId(project.getId());
        AppProxy.get().queryRepository(request, new AsyncCallback<RpcResult<QueryRepositoryResponse>>() {
            @Override
            public void onFailure(Throwable caught) {

            }

            @Override
            public void onSuccess(RpcResult<QueryRepositoryResponse> result) {
                if (result.isSuccess()) {
                    setData(result.getData().getProjects().get(0));
                    fireEvent(CommonEvent.updateEvent(project));
                }
            }
        });
    }

    private void editorFile(RepoItem repoItem) {

        final ReadRepoFileRequest request = new ReadRepoFileRequest();
        request.setToHtml(false);
        request.setProjectId(project.getId());
        request.setFilePathName(repoItem.getPath());
        AppProxy.get().readRepoFile(request, new AsyncCallback<RpcResult<ReadRepoFileResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<ReadRepoFileResponse> result) {
                if (result.isSuccess()) {
                    showEditor();
                    String text = result.getData().getText();
                    // Simple check for binary content (null bytes)
                    if (text.contains("\0")) {
                        editor.setValue("Binary file - cannot display content.");
                        editor.setReadOnly(true);
                    } else {
                        editor.setValue(text);
                        editor.setReadOnly(true);
                        editor.setMode(guessFileMode(request.getFilePathName()));
                        editor.redisplay();
                    }
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    private AceEditorMode guessFileMode(String filePathName) {
        String suffix = StringUtil.suffix(filePathName).toLowerCase();

        AceEditorMode aceEditorMode = format.get(suffix);
        if (aceEditorMode == null) {
            return AceEditorMode.TEXT;
        }
        return aceEditorMode;
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        initEditor();
        contentPanel.clear();
    }

    private void loadRefs() {
        files.clear();
        QueryRepoRefsRequest request = new QueryRepoRefsRequest();
        request.setProjectId(project.getId());
        AppProxy.get().queryRepoRefs(request, new AsyncCallback<RpcResult<QueryRepoRefsResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryRepoRefsResponse> result) {

                if (!result.isSuccess()) {
                    ClientContext.get().toast(0, 0, result.getMessage());
                    return;
                }
                List<GitRef> refs = result.getData().getRefs();
                if (refs == null || refs.isEmpty()) {
                    // No branches? No tags? Show the "How to Push" instructions.
                    String html = result.getData().getInstruction();
                    showEmptyRepoPanel(html);
                } else {
                    showFilesPanel();
                    int branchCount = 0;
                    int tagCount = 0;
                    for (GitRef ref : refs) {
                        if (ref.getKind().equals(0)) {
                            branchCount++;
                        } else {
                            tagCount++;
                        }
                    }
                    btnBranch.setText(branchCount + "分支");
                    btnTag.setText(tagCount + "标签");
                    ddlRef.setData(result.getData());
                }
            }
        });

    }

    private void showEditor() {
        root.setWidgetSize(opBar, 60);
        root.setWidgetSize(pathBar, 50);
        root.setWidgetSize(infoBar, 50);
        contentPanel.clear();
        contentPanel.add(editor);
        contentPanel.setWidgetLeftRight(editor, 10, Style.Unit.PX, 10, Style.Unit.PX);
        contentPanel.setWidgetTopBottom(editor, 0, Style.Unit.PX, 10, Style.Unit.PX);
        initEditor();
    }

    private void showFilesPanel() {
        root.setWidgetSize(opBar, 60);
        root.setWidgetSize(pathBar, 50);
        root.setWidgetSize(infoBar, 50);
        contentPanel.clear();
        contentPanel.add(fileContainer);
        contentPanel.setWidgetLeftRight(fileContainer, 0, Style.Unit.PX, 0, Style.Unit.PX);
        contentPanel.setWidgetTopBottom(fileContainer, 0, Style.Unit.PX, 0, Style.Unit.PX);

    }

    /**
     * 这是一个空的仓库　可以从别个Git Server拉取　或者上传 push
     *
     * @param html
     */
    private void showEmptyRepoPanel(String html) {
        if (importRepoPanel == null) {
            importRepoPanel = new ImportRepoPanel();
            importRepoPanel.addCommonHandler(event -> {
                if (event.isUpdate()) {
                    reload();
                }
            });
        }
        root.setWidgetSize(opBar, 0);
        root.setWidgetSize(infoBar, 0);
        root.setWidgetSize(pathBar, 0);
        contentPanel.clear();
        contentPanel.add(messagePanel);
        contentPanel.setWidgetLeftRight(messagePanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
        contentPanel.setWidgetTopBottom(messagePanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
        msgContainer.clear();
        msgContainer.add(importRepoPanel);
        importRepoPanel.setData(project);
        HTML html1 = new HTML(html);
        html1.addStyleName("markdown-body");
        msgContainer.add(html1);
    }

    /**
     * 初始化编辑器
     */
    private void initEditor() {
        if (!initialize) {
            editor.startEditor();

            editor.setShowPrintMargin(false);
            editor.setFontSize(12);
            editor.setUseWorker(false);
            editor.setShowGutter(true);
            editor.setUseWrapMode(true);
            editor.redisplay();
            editor.setFontSize("1.2rem");
            initialize = true;

            AceCommandDescription ctrlSaveCommand = new AceCommandDescription("save", new AceCommandDescription.ExecAction() {
                @Override
                public Object exec(AceEditor aceEditor) {
                    return true;
                }
            });
            ctrlSaveCommand.withBindKey("Ctrl-S", "Cmd-S");
            editor.addCommand(ctrlSaveCommand);

        }
        editor.redisplay();
    }

    private void loadDir(String ref, String path) {
        final QueryRepoFilesRequest request = new QueryRepoFilesRequest();
        request.setProjectId(project.getId());
        request.setPath(path);
        request.setRef(ref);
        AppProxy.get().queryRepoFiles(request, new AsyncCallback<RpcResult<QueryRepoFilesResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryRepoFilesResponse> result) {
                if (result.isSuccess()) {
                    showFilesPanel();
                    renderFiles(result.getData());
                    renderPath(request.getPath());
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    private void toUI() {

        RepositoryStatus repositoryStatus = RepositoryStatus.fromCode(project.getStatus());
        switch (repositoryStatus) {
            case PS_INIT:
                showEmptyRepoPanel("");
                break;
            case PS_IMPORTING:
                //正在导入仓库　
                showImportingPanel();
                break;
            case PS_NORMAL:
            case PS_DELETE:
            case PS_UNKNOWN:
            case PS_ARCHIVE:
            default:
                showFilesPanel();
                files.clear();
                btnClone.setData(project);
                loadRefs();
        }
    }

    private void showImportingPanel() {
        if (importPanel == null) {
            importPanel = new ImportingPanel();
            importPanel.addCommonHandler(new CommonEventHandler() {
                @Override
                public void onCommonEvent(CommonEvent event) {
                    if (event.isReload()) {
                        reload();
                    }
                }
            });
        }
        root.setWidgetSize(opBar, 0);
        root.setWidgetSize(infoBar, 0);
        root.setWidgetSize(pathBar, 0);
        contentPanel.clear();
        contentPanel.add(messagePanel);
        contentPanel.setWidgetLeftRight(messagePanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
        contentPanel.setWidgetTopBottom(messagePanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
        msgContainer.clear();
        msgContainer.add(importPanel);
        importPanel.setData(project);
    }

    private void renderPath(String path) {
        paths.clear();
        Head head = new Head();
        head.addStyleName(style.link());
        head.setText(project.getName());
        head.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                loadDir(ddlRef.getValue(), "");
            }
        });
        paths.add(head);
        String cumulativePath = "";
        List<String> segs = StringUtil.splitIgnoreBlank(path, "/");
        for (String seg : segs) {
            paths.add(new Label("/"));

            // Build path: "a", then "a/b", then "a/b/c"
            if (!cumulativePath.isEmpty()) {
                cumulativePath += "/";
            }
            cumulativePath += seg;

            AiLabel link = new AiLabel(seg);
            link.addStyleName(style.link());
            link.setData(cumulativePath); // Store the normalized path
            link.addClickHandler(linkClicked);
            paths.add(link);
        }
    }

    private void renderFiles(QueryRepoFilesResponse data) {
        List<RepoItem> items = data.getItems();
        RepoItem dirInfo = data.getCurrentDirInfo();
        files.clear();
        Collections.sort(items, (o1, o2) -> {
            if (o1.isDir() && o2.isDir()) {
                return o1.getName().compareTo(o2.getName());
            } else if (o1.isDir()) {
                return -1;
            } else if (o2.isDir()) {
                return 1;
            } else {
                return o1.getName().compareTo(o2.getName());
            }
        });

        dirLastUser.setText(dirInfo.getAuthor());
        dirLastSummary.setText(dirInfo.getSummary());
        dirLastModify.setText(StringUtil.toRelativeTime(dirInfo.getDate()));

        for (RepoItem item : items) {
            RepoFileItem item2 = new RepoFileItem();
            item2.setData(item);

            item2.addCommonHandler(itemHandler);
            files.add(item2);
        }
    }

    private void previewImage(RepoItem repoItem) {
        // Hide editor
        showFilesPanel();
        // Create a URL to your raw file servlet (recommended approach)
        String imageUrl = GWT.getHostPageBaseURL() + "raw/" + project.getOwnerName() + "/" + project.getName() + "/" + repoItem.getPath();

        Image image = new Image(imageUrl);
        image.addStyleName(style.imagePreview()); // Add some CSS to center it and limit size

        files.clear();
        files.add(image);
    }

    interface SStyle extends CssResource {

        String link();

        String box();

        String imagePreview();

        String msg();

        String box1();

        String scroll();

        String box2();

        String files();
    }

    interface ProjectCodeFrameUiBinder extends UiBinder<DockLayoutPanel, RepositoryCodeFrame> {
    }

    CommonEventHandler itemHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent event) {
            RepoItem repoItem = event.getValue();
            if (repoItem.isDir()) {
                loadDir(ddlRef.getValue(), repoItem.getPath());
            } else {
                String path = repoItem.getPath().toLowerCase();
                if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".gif") || path.endsWith(".jpeg")) {
                    previewImage(repoItem);
                } else {
                    editorFile(repoItem);
                }
            }
        }
    };


    private final ClickHandler linkClicked = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            AiLabel label = (AiLabel) event.getSource();
            String relPath = (String) label.getData();
            loadDir(ddlRef.getValue(), relPath);
        }
    };


}