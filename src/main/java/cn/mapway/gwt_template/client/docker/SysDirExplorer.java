package cn.mapway.gwt_template.client.docker;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.rpc.docker.QuerySysDirRequest;
import cn.mapway.gwt_template.shared.rpc.docker.QuerySysDirResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.ResItem;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.AiAnchor;
import cn.mapway.ui.client.widget.AiLabel;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.client.widget.panel.MessagePanel;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SysDirExplorer extends CommonEventComposite {
    private static final SysDirExplorerUiBinder ourUiBinder = GWT.create(SysDirExplorerUiBinder.class);
    private static Dialog<SysDirExplorer> dialog;
    @UiField
    SaveBar saveBar;
    @UiField
    HTMLPanel list;
    @UiField
    SStyle style;
    @UiField
    HTMLPanel pathBar;
    private String currentPath = "";

    public SysDirExplorer() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Dialog<SysDirExplorer> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<SysDirExplorer> createOne() {
        return new Dialog<>(new SysDirExplorer(), "系统目录浏览器");
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            fireEvent(CommonEvent.selectEvent(currentPath));
        } else {
            fireEvent(event);
        }
    }

    public void loadPath(String path) {
        saveBar.enableSave(false);
        if (StringUtil.isNotBlank(path)) {
            currentPath = path;
        } else {
            currentPath = "/";
        }
        QuerySysDirRequest request = new QuerySysDirRequest();
        request.setPath(currentPath);
        AppProxy.get().querySysDir(request, new AsyncCallback<RpcResult<QuerySysDirResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QuerySysDirResponse> result) {
                if (result.isSuccess()) {
                    currentPath = result.getData().getPath();
                    saveBar.enableSave(true);
                    list.clear();
                    Collections.sort(result.getData().getDirs(), new Comparator<ResItem>() {
                        @Override
                        public int compare(ResItem resItem, ResItem t1) {
                            return resItem.getPathName().compareTo(t1.getPathName());
                        }
                    });
                    for (ResItem item : result.getData().getDirs()) {
                        AiLabel label = new AiLabel();
                        label.setStyleName(style.item());
                        label.setText(item.getPathName());
                        label.setData(item);
                        label.addClickHandler(new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {
                                if (currentPath.length() == 1) {
                                    loadPath(currentPath + item.getPathName());
                                } else {
                                    loadPath(currentPath + "/" + item.getPathName());
                                }
                            }
                        });
                        list.add(label);
                    }
                    renderPath(currentPath);
                    if (result.getData().getDirs().isEmpty()) {
                        list.add(new MessagePanel().setText("没有子目录了"));
                    }
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    private void renderPath(String currentPath) {
        saveBar.msg(currentPath);
        pathBar.clear();
        List<String> strings = StringUtil.splitIgnoreBlank(currentPath, "/");
        Anchor anchor = new Anchor("/");
        anchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                loadPath("/");
            }
        });
        pathBar.add(anchor);
        int index = 0;
        String path = "";
        for (String pathSeg : strings) {
            if (index > 0) {
                pathBar.add(new Label("/"));
                path = path + "/" + pathSeg;
            } else {
                path = "/" + pathSeg;
            }

            AiAnchor seg = new AiAnchor(pathSeg);
            seg.setData(path);

            seg.addClickHandler(segClicked);
            pathBar.add(seg);
            index++;
        }
    }

    interface SStyle extends CssResource {

        String item();

        String list();

        String pathbar();
    }

    interface SysDirExplorerUiBinder extends UiBinder<DockLayoutPanel, SysDirExplorer> {
    }

    ClickHandler segClicked = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            AiAnchor anchor = (AiAnchor) event.getSource();
            String path = anchor.getData().toString();
            loadPath(path);
        }
    };


}