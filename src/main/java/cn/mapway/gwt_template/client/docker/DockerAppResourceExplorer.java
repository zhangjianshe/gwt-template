package cn.mapway.gwt_template.client.docker;

import cn.mapway.gwt_template.shared.db.DockerAppEntity;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public class DockerAppResourceExplorer extends CommonEventComposite implements IData<DockerAppEntity> {
    private static final DockerAppResourceExplorerUiBinder ourUiBinder = GWT.create(DockerAppResourceExplorerUiBinder.class);
    private DockerAppEntity appEntity;

    public DockerAppResourceExplorer() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public DockerAppEntity getData() {
        return appEntity;
    }

    @Override
    public void setData(DockerAppEntity obj) {
        appEntity = obj;
        toUI();
    }

    private void toUI() {
    }

    interface DockerAppResourceExplorerUiBinder extends UiBinder<DockLayoutPanel, DockerAppResourceExplorer> {
    }
}