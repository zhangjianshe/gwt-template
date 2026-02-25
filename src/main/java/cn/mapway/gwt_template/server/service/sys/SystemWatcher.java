package cn.mapway.gwt_template.server.service.sys;

import org.springframework.stereotype.Service;

/**
 * 系统自动化任务监控
 */
@Service
public class SystemWatcher {
    private Double ticker = 0.;

    /**
     * 每两秒钟发送一个消息
     * Fixed delay ensures it runs 2 seconds after the previous execution finishes
     *//*
    @Scheduled(fixedDelay = 2000)
    public void autoSendGitTestMessage() {
        if (ticker >= 100) {
            ticker = 0.; // Reset for testing loop
        }
        ticker = ticker + 5.;

        CommonMessage<GitNotifyMessage> msg = new CommonMessage<>();
        msg.topic = AppConstant.TOPIC_GIT_IMPORT;

        GitNotifyMessage notifyMessage = new GitNotifyMessage();
        notifyMessage.message = "System Heartbeat: Tracking Project...";
        notifyMessage.progress = ticker;
        notifyMessage.phase = AppConstant.MESSAGE_PHASE_IMPORT;
        notifyMessage.type = AppConstant.MESSAGE_TYPE_PROGRESS; // Using 'kind' based on your previous GitRepoService logic
        notifyMessage.projectId = "65ca892c16b74bbcaa828942bd543f9d";
        msg.data = notifyMessage;

        GitNotifyWebSocket.sendMessage(6L, Json.toJson(msg));
    }*/
}
