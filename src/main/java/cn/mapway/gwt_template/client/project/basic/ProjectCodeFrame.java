package cn.mapway.gwt_template.client.project.basic;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.VwProjectEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryRepoFilesRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryRepoFilesResponse;
import cn.mapway.gwt_template.shared.rpc.project.RepoItem;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;

import java.util.Collections;
import java.util.List;

public class ProjectCodeFrame extends CommonEventComposite implements IData<VwProjectEntity> {
    private static final ProjectCodeFrameUiBinder ourUiBinder = GWT.create(ProjectCodeFrameUiBinder.class);
    @UiField
    HorizontalPanel paths;
    @UiField
    HTMLPanel files;
    private VwProjectEntity project;

    public ProjectCodeFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public VwProjectEntity getData() {
        return project;
    }

    @Override
    public void setData(VwProjectEntity obj) {
        project = obj;
        toUI();
    }

    private void toUI() {
        files.clear();
        QueryRepoFilesRequest request = new QueryRepoFilesRequest();
        request.setProjectId(project.getId());
        request.setPath("");
        AppProxy.get().queryRepoFiles(request, new AsyncCallback<RpcResult<QueryRepoFilesResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryRepoFilesResponse> result) {
                if (result.isSuccess()) {
                    renderFiles(result.getData().getItems());
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    private void renderFiles(List<RepoItem> items) {
        Collections.sort(items, (o1, o2) -> {
            if (o1.isDir() && o2.isDir()) {
                return o1.getPathName().compareTo(o2.getPathName());
            } else if (o1.isDir()) {
                return -1;
            } else if (o2.isDir()) {
                return 1;
            } else {
                return o1.getPathName().compareTo(o2.getPathName());
            }
        });
        for (RepoItem item : items) {
            RepoFileItem item2 = new RepoFileItem();
            item2.setData(item);
            files.add(item2);
        }
    }

    interface ProjectCodeFrameUiBinder extends UiBinder<DockLayoutPanel, ProjectCodeFrame> {
    }
}