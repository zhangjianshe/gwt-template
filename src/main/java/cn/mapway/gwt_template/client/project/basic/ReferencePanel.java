package cn.mapway.gwt_template.client.project.basic;

import cn.mapway.gwt_template.shared.rpc.project.QueryRepoRefsResponse;
import cn.mapway.gwt_template.shared.rpc.project.git.GitRef;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.SearchBox;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.client.widget.list.CommonList;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;


/**
 * 仓库分支和版本选择
 */
public class ReferencePanel extends CommonEventComposite implements IData<QueryRepoRefsResponse> {
    private static final ReferencePanelUiBinder ourUiBinder = GWT.create(ReferencePanelUiBinder.class);
    private static Popup<ReferencePanel> popup;
    @UiField
    SearchBox searchBox;
    @UiField
    CommonList branchList;
    @UiField
    CommonList tagList;
    @UiField
    TabLayoutPanel tab;
    private QueryRepoRefsResponse data;

    public ReferencePanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
        searchBox.addValueChangeHandler(event -> filter(event.getValue()));
    }

    public static Popup<ReferencePanel> getPopup(boolean reuse) {
        if (reuse) {
            if (popup == null) {
                popup = createOne();
            }
            return popup;
        } else {
            return createOne();
        }
    }

    private static Popup<ReferencePanel> createOne() {
        ReferencePanel panel = new ReferencePanel();
        return new Popup<>(panel);
    }

    private void filter(String value) {
        if (StringUtil.isBlank(value)) {
            toUI();
        } else {
            branchList.clear();
            tagList.clear();
            tab.selectTab(0, true);
            for (GitRef ref : data.getRefs()) {
                if (ref.getName().contains(value)) {
                    branchList.addItem("", ref.getName(), ref);
                }
            }
        }
    }

    @Override
    public QueryRepoRefsResponse getData() {
        return data;
    }

    @Override
    public void setData(QueryRepoRefsResponse obj) {
        data = obj;
        toUI();
    }

    private void toUI() {
        tagList.clear();
        branchList.clear();
        for (GitRef gitRef : data.getRefs()) {
            if (gitRef.getKind().equals(0)) {
                branchList.addItem("", gitRef.getName(), gitRef);
            } else {
                tagList.addItem("", gitRef.getName(), gitRef);
            }
        }
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(500, 600);
    }

    @UiHandler("tagList")
    public void tagListCommon(CommonEvent event) {
        fireEvent(CommonEvent.selectEvent(event.getValue()));
    }

    @UiHandler("branchList")
    public void branchListCommon(CommonEvent event) {
        fireEvent(CommonEvent.selectEvent(event.getValue()));
    }

    interface ReferencePanelUiBinder extends UiBinder<DockLayoutPanel, ReferencePanel> {
    }
}