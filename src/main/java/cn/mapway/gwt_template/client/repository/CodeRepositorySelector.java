package cn.mapway.gwt_template.client.repository;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.VwRepositoryEntity;
import cn.mapway.gwt_template.shared.rpc.dev.QueryRepositoryRequest;
import cn.mapway.gwt_template.shared.rpc.dev.QueryRepositoryResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.SearchBox;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.client.widget.list.List;
import cn.mapway.ui.client.widget.list.ListItem;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;

public class CodeRepositorySelector extends CommonEventComposite {
    private static final CodeRepositorySelectorUiBinder ourUiBinder = GWT.create(CodeRepositorySelectorUiBinder.class);
    private static Dialog<CodeRepositorySelector> dialog;
    VwRepositoryEntity selectedRepo = null;
    @UiField
    SaveBar saveBar;
    @UiField
    SearchBox searchBox;
    @UiField
    List list;

    public CodeRepositorySelector() {
        initWidget(ourUiBinder.createAndBindUi(this));
        searchBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                load(event.getValue());
            }
        });
    }

    public static Dialog<CodeRepositorySelector> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<CodeRepositorySelector> createOne() {
        CodeRepositorySelector selector = new CodeRepositorySelector();
        return new Dialog<>(selector, "选择代码仓库");
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        selectedRepo = null;
        updateUI();
    }

    private void updateUI() {
        saveBar.setEnableSave(selectedRepo != null);
    }

    public void load(String nameFilter) {
        QueryRepositoryRequest request = new QueryRepositoryRequest();
        request.setNameFilter(nameFilter);
        AppProxy.get().queryRepository(request, new AsyncCallback<RpcResult<QueryRepositoryResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                saveBar.msg(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryRepositoryResponse> result) {
                if (result.isSuccess()) {
                    renderRepository(result.getData());
                } else {
                    saveBar.msg(result.getMessage());
                }
            }
        });
    }

    private void renderRepository(QueryRepositoryResponse data) {
        list.clear();
        for (VwRepositoryEntity repository : data.getRepositories()) {
            ListItem item = new ListItem();
            item.setIcon(Fonts.APPS);
            item.setText(repository.getName()+"["+ repository.getFullName() + "](" + repository.getOwnerName() + ")");
            item.setData(repository);
            Label label = new Label(String.valueOf(repository.getMemberCount()));
            label.getElement().getStyle().setTextAlign(Style.TextAlign.RIGHT);
            item.appendRight(label, 50);
            list.addItem(item);
        }
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            fireEvent(CommonEvent.selectEvent(selectedRepo));
        } else {
            fireEvent(event);
        }
    }

    @UiHandler("list")
    public void listCommon(CommonEvent event) {
        if (event.isSelect()) {
            ListItem listItem = event.getValue();
            selectedRepo = (VwRepositoryEntity) listItem.getData();
            updateUI();
            saveBar.message(selectedRepo.getName());
        }
    }

    interface CodeRepositorySelectorUiBinder extends UiBinder<DockLayoutPanel, CodeRepositorySelector> {
    }
}