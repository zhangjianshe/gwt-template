package cn.mapway.gwt_template.client.project;

import cn.mapway.gwt_template.client.project.basic.ProjectCodeFrame;
import cn.mapway.gwt_template.client.project.basic.ProjectFlowPanel;
import cn.mapway.gwt_template.client.widget.IconButton;
import cn.mapway.gwt_template.shared.db.VwProjectEntity;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.BaseAbstractModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.shared.CommonEventHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.LayoutPanel;

/**
 * 项目视图
 */
@ModuleMarker(
        name = "项目视图",
        value = ProjectView.MODULE_CODE,
        unicode = Fonts.PROJECT,
        summary = "项目详细信息",
        order = 100
)
public class ProjectView extends BaseAbstractModule implements IData<VwProjectEntity> {
    public final static String MODULE_CODE = "git_project_view";
    private static final ProjectViewUiBinder ourUiBinder = GWT.create(ProjectViewUiBinder.class);
    @UiField
    HorizontalPanel buttons;
    @UiField
    IconButton btnSetting;
    @UiField
    IconButton btnCode;
    @UiField
    IconButton btnWiki;
    @UiField
    IconButton btnBasic;
    @UiField
    LayoutPanel content;
    IconButton selected = null;
    ProjectFlowPanel projectFlowPanel;
    ProjectCodeFrame projectCodeFrame;
    private VwProjectEntity vwProject;

    public ProjectView() {
        initWidget(ourUiBinder.createAndBindUi(this));
        btnBasic.setValue(Fonts.BOOK, "项目看板").setData(Fonts.BOOK);
        btnCode.setValue(Fonts.CODE, "代码").setData(Fonts.CODE);
        btnWiki.setValue(Fonts.TXT, "WIKI").setData(Fonts.TXT);
        btnSetting.setValue(Fonts.SETTING, "设置").setData(Fonts.SETTING);

        CommonEventHandler buttonClickHandler = event -> {
            if (event.isSelect()) {
                String data = event.getValue();
                if (data.equals(Fonts.BOOK)) {
                    selectButton(btnBasic);
                    gotoBasic();
                } else if (data.equals(Fonts.CODE)) {
                    selectButton(btnCode);
                    gotoCode();
                } else if (data.equals(Fonts.SETTING)) {
                    selectButton(btnSetting);
                } else if (data.equals(Fonts.TXT)) {
                    selectButton(btnWiki);
                    gotoWiki();
                }
            }
        };
        btnBasic.addCommonHandler(buttonClickHandler);
        btnCode.addCommonHandler(buttonClickHandler);
        btnWiki.addCommonHandler(buttonClickHandler);
        btnSetting.addCommonHandler(buttonClickHandler);
    }

    private void gotoBasic() {
        if (projectFlowPanel == null) {
            projectFlowPanel = new ProjectFlowPanel();
        }
        content.clear();
        content.add(projectFlowPanel);
        content.setWidgetLeftRight(projectFlowPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
        content.setWidgetTopBottom(projectFlowPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
        projectFlowPanel.setData(vwProject);
    }

    private void gotoWiki() {
    }

    private void gotoCode() {
        if (projectCodeFrame == null) {
            projectCodeFrame = new ProjectCodeFrame();
        }
        content.clear();
        content.add(projectCodeFrame);
        content.setWidgetLeftRight(projectCodeFrame, 0, Style.Unit.PX, 0, Style.Unit.PX);
        content.setWidgetTopBottom(projectCodeFrame, 0, Style.Unit.PX, 0, Style.Unit.PX);
        projectCodeFrame.setData(vwProject);
    }

    public void selectButton(IconButton button) {
        if (selected != null) {
            selected.setSelect(false);
        }
        selected = button;
        if (selected != null) {
            selected.setSelect(true);
        }
    }

    @Override
    public VwProjectEntity getData() {
        return vwProject;
    }

    @Override
    public void setData(VwProjectEntity obj) {
        vwProject = obj;
        toUI();
    }

    private void toUI() {
        btnBasic.select();
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        super.initialize(parentModule, parameter);

        return true;
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    interface ProjectViewUiBinder extends UiBinder<DockLayoutPanel, ProjectView> {
    }
}