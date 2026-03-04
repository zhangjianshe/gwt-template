package cn.mapway.gwt_template.client.repository.basic;

import cn.mapway.gwt_template.client.user.ClientWebSocket;
import cn.mapway.gwt_template.client.user.GitNotifyMessage;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.VwRepositoryEntity;
import cn.mapway.gwt_template.shared.rpc.repository.RepositoryStatus;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import jsinterop.base.Js;

import java.util.Objects;

public class ImportingPanel extends CommonEventComposite implements IData<VwRepositoryEntity> {
    private static final ImportingPanelUiBinder ourUiBinder = GWT.create(ImportingPanelUiBinder.class);
    @UiField
    Label lbMessage;
    private VwRepositoryEntity project;

    public ImportingPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
        ClientWebSocket.get().connectToServer();
    }

    @Override
    public VwRepositoryEntity getData() {
        return project;
    }

    @Override
    public void setData(VwRepositoryEntity obj) {
        project = obj;
        RepositoryStatus repositoryStatus = RepositoryStatus.fromCode(project.getStatus());
        if (repositoryStatus == RepositoryStatus.PS_IMPORTING) {
            lbMessage.setText("正在导入 进度:");
        } else {
            lbMessage.setText(repositoryStatus.getName());
        }

    }

    @Override
    protected void onLoad() {
        super.onLoad();
        registerBusEvent(AppConstant.TOPIC_GIT_IMPORT);
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        clearBusEvent();
    }

    @Override
    public void onEvent(String topic, int type, Object event) {
        if (Objects.equals(topic, AppConstant.TOPIC_GIT_IMPORT)) {
            GitNotifyMessage info = Js.uncheckedCast(event);
            if (info.phase.equals(AppConstant.MESSAGE_PHASE_IMPORT)) {
                if (info.type.equals(AppConstant.MESSAGE_TYPE_SUCCESS)) {
                    // 延迟一秒让用户看清“100%”，然后通知外部刷新
                    Timer timer = new Timer() {
                        @Override
                        public void run() {
                            fireEvent(CommonEvent.reloadEvent(project));
                        }
                    };
                    timer.schedule(1000);
                } else if (info.type.equals(AppConstant.MESSAGE_TYPE_ERROR)) {
                    lbMessage.setText(info.message);
                } else if (info.type.equals(AppConstant.MESSAGE_TYPE_PROGRESS)) {
                    lbMessage.setText("目前进度" + info.progress + "%" + " " + info.message);
                }
            }
        }
    }

    interface ImportingPanelUiBinder extends UiBinder<HTMLPanel, ImportingPanel> {
    }
}