package cn.mapway.gwt_template.client.repository.basic;

import cn.mapway.gwt_template.shared.db.VwProjectEntity;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

public class RepositoryDetailPanel extends CommonEventComposite implements IData<VwProjectEntity> {
    private static final ProjectDetailPanelUiBinder ourUiBinder = GWT.create(ProjectDetailPanelUiBinder.class);
    @UiField
    FontIcon btnSetup;
    @UiField
    Label lbAbout;
    private VwProjectEntity project;

    public RepositoryDetailPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
        btnSetup.setIconUnicode(Fonts.SETTING);
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
        lbAbout.setText(project.getSummary());
    }

    @UiHandler("btnSetup")
    public void btnSetupClick(ClickEvent event) {
        Dialog<RepositoryDetailEditor> dialog = RepositoryDetailEditor.getDialog(true);
        dialog.addCommonHandler(event1 -> {
            if (event1.isUpdate()) {
                VwProjectEntity pro = event1.getValue();
                fireEvent(CommonEvent.updateEvent(pro));
            }
            dialog.hide();
        });
        dialog.getContent().setData(project);
        dialog.center();
    }

    interface ProjectDetailPanelUiBinder extends UiBinder<HTMLPanel, RepositoryDetailPanel> {
    }
}