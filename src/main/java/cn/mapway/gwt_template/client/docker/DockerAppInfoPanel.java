package cn.mapway.gwt_template.client.docker;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.rpc.docker.QueryDockerAppInfoRequest;
import cn.mapway.gwt_template.shared.rpc.docker.QueryDockerAppInfoResponse;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;

public class DockerAppInfoPanel extends CommonEventComposite implements IData<DockerAppData> {
    private static final DockerAppInfoPanelUiBinder ourUiBinder = GWT.create(DockerAppInfoPanelUiBinder.class);
    @UiField
    HTMLPanel resultPanel;
    private DockerAppData dockerAppData;

    public DockerAppInfoPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public DockerAppData getData() {

        return dockerAppData;
    }

    @Override
    public void setData(DockerAppData obj) {
        dockerAppData = obj;
        toUI();
    }

    private void toUI() {
        QueryDockerAppInfoRequest request = new QueryDockerAppInfoRequest();
        request.setDockerAppId(dockerAppData.getAppEntity().getId());
        AppProxy.get().queryDockerAppInfo(request, new AsyncCallback<RpcResult<QueryDockerAppInfoResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryDockerAppInfoResponse> result) {
                if (result.isSuccess()) {
                    resultPanel.add(new HTML("<pre>" + result.getData().getStatus() + "</pre>"));
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    interface DockerAppInfoPanelUiBinder extends UiBinder<DockLayoutPanel, DockerAppInfoPanel> {
    }
}