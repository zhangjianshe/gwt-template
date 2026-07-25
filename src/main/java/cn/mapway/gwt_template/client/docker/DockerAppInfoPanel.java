package cn.mapway.gwt_template.client.docker;

import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public class DockerAppInfoPanel extends CommonEventComposite implements IData<DockerAppData> {
    private static final DockerAppInfoPanelUiBinder ourUiBinder = GWT.create(DockerAppInfoPanelUiBinder.class);
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

    }
    interface DockerAppInfoPanelUiBinder extends UiBinder<DockLayoutPanel, DockerAppInfoPanel> {
    }
}