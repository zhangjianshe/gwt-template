package cn.mapway.gwt_template.client.repository.basic;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DevRepositoryEntity;
import cn.mapway.gwt_template.shared.db.VwRepositoryEntity;
import cn.mapway.gwt_template.shared.rpc.dev.UpdateRepositoryRequest;
import cn.mapway.gwt_template.shared.rpc.dev.UpdateRepositoryResponse;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.AiTextBox;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.TextArea;

/**
 * 编辑项目详情
 */
public class RepositoryDetailEditor extends CommonEventComposite implements IData<VwRepositoryEntity> {
    private static final RepositoryEditorUiBinder ourUiBinder = GWT.create(RepositoryEditorUiBinder.class);
    private static Dialog<RepositoryDetailEditor> dialog;
    @UiField
    SaveBar saveBar;
    @UiField
    AiTextBox txtFullName;
    @UiField
    AiTextBox txtTags;
    @UiField
    TextArea txtSummary;
    private VwRepositoryEntity repository;

    public RepositoryDetailEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Dialog<RepositoryDetailEditor> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<RepositoryDetailEditor> createOne() {
        RepositoryDetailEditor editor = new RepositoryDetailEditor();
        return new Dialog<>(editor, "编辑项目");
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(600, 350);
    }

    @Override
    public VwRepositoryEntity getData() {
        return repository;
    }

    @Override
    public void setData(VwRepositoryEntity obj) {
        assert obj != null;
        repository = obj;
        toUI();
    }

    private void toUI() {
        txtFullName.setValue(repository.getFullName());
        txtTags.setValue(repository.getTags());
        txtSummary.setValue(repository.getSummary());
    }


    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            DevRepositoryEntity temp = new DevRepositoryEntity();
            temp.setId(repository.getId());
            temp.setFullName(txtFullName.getValue());
            temp.setTags(txtTags.getValue());
            temp.setSummary(txtSummary.getValue());
            doSave(temp);
        } else {
            fireEvent(event);
        }
    }

    private void doSave(DevRepositoryEntity temp) {
        UpdateRepositoryRequest request = new UpdateRepositoryRequest();
        request.setRepository(temp);
        AppProxy.get().updateRepository(request, new AsyncCallback<>() {
            @Override
            public void onFailure(Throwable caught) {
                saveBar.msg(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateRepositoryResponse> result) {
                if (result.isSuccess()) {
                    fireEvent(CommonEvent.updateEvent(result.getData().getRepository()));
                } else {
                    saveBar.msg(result.getMessage());
                }
            }
        });
    }

    interface RepositoryEditorUiBinder extends UiBinder<DockLayoutPanel, RepositoryDetailEditor> {
    }
}