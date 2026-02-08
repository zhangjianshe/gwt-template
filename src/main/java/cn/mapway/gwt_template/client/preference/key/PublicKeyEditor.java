package cn.mapway.gwt_template.client.preference.key;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.SysUserKeyEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateUserKeyRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateUserKeyResponse;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.AiTextBox;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.datepicker.client.DateBox;

import java.util.Date;
import java.util.List;

public class PublicKeyEditor extends CommonEventComposite implements IData<SysUserKeyEntity> {

    private static final PublicKeyEditorUiBinder ourUiBinder = GWT.create(PublicKeyEditorUiBinder.class);
    private static Dialog<PublicKeyEditor> dialog;
    @UiField
    SaveBar saveBar;
    @UiField
    AiTextBox txtName;
    @UiField
    DateBox dateExpire;
    @UiField
    TextArea txtKey;
    @UiField
    Button btnRemoveExpire;
    private SysUserKeyEntity data;

    public PublicKeyEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
        txtKey.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                String key = txtKey.getText();
                List<String> segs = StringUtil.splitIgnoreBlank(key, " ");
                //猜测名字
                if (segs.size() > 1) {
                    String name = segs.get(segs.size() - 1);
                    txtName.setValue(name);
                }
            }
        });
        dateExpire.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat(StringUtil.FULL_DATETIME_FORMAT)));
    }

    public static Dialog<PublicKeyEditor> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<PublicKeyEditor> createOne() {
        PublicKeyEditor editor = new PublicKeyEditor();
        return new Dialog<>(editor, "编辑公钥");
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(600, 450);
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            doSave();
        } else {
            fireEvent(event);
        }
    }

    @UiHandler("btnRemoveExpire")
    public void btnRemoveExpireClick(ClickEvent event) {
        dateExpire.setValue(null);
    }

    private void doSave() {
        SysUserKeyEntity key = new SysUserKeyEntity();
        key.setKey(txtKey.getValue());
        key.setName(txtName.getValue());
        if (dateExpire.getValue() != null) {
            key.setExpiredTime(dateExpire.getValue().getTime());
        }
        if (StringUtil.isBlank(key.getName())) {
            saveBar.msg("请为公钥提供一个名称方便识别");
            return;
        }

        UpdateUserKeyRequest request = new UpdateUserKeyRequest();
        request.setKey(key);
        AppProxy.get().updateUserKey(request, new AsyncCallback<RpcResult<UpdateUserKeyResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                saveBar.msg(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateUserKeyResponse> result) {
                if (result.isSuccess()) {
                    fireEvent(CommonEvent.okEvent(null));
                } else {
                    saveBar.msg(result.getMessage());
                }
            }
        });
    }

    @Override
    public SysUserKeyEntity getData() {
        return data;
    }

    @Override
    public void setData(SysUserKeyEntity obj) {
        data = obj;
        if (data == null) {
            data = new SysUserKeyEntity();
            data.setName("key name");
            dateExpire.setValue(null);
        }
        toUI();
    }

    private void toUI() {
        txtName.setValue(data.getName());
        txtKey.setValue(data.getKey());
        if (data.getExpiredTime() == null || data.getExpiredTime() <= 0) {
            dateExpire.setValue(null);
        } else {
            dateExpire.setValue(new Date(data.getExpiredTime()));
        }
    }

    interface PublicKeyEditorUiBinder extends UiBinder<DockLayoutPanel, PublicKeyEditor> {
    }
}