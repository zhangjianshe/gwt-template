package cn.mapway.gwt_template.server.service.log;

import cn.mapway.gwt_template.shared.db.SysLogEntity;
import cn.mapway.gwt_template.shared.rpc.log.LogAction;
import cn.mapway.gwt_template.shared.rpc.log.LogLevel;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;

@Slf4j
@Service
public class SysLogService {
    @Resource
    Dao dao;

    public void logAction(LogLevel level, Long userId, String userName, LogAction action, String content) {
        if (action == null) {
            log.warn("[LOG] 记录系统日至没有日至活动标题");
            return;
        }
        logAction(level, userId, userName, action.getAction(), content);
    }

    public void logAction(LogLevel level, Long userId, String userName, String action, String content) {

        if (Strings.isBlank(action)) {
            log.warn("[LOG] 记录系统日至没有日至活动标题");
            return;
        }
        if (content == null) {
            content = "";
        }
        SysLogEntity logEntity = new SysLogEntity();
        logEntity.setAction(action);
        logEntity.setId(R.UU16());
        logEntity.setUserId(userId);
        logEntity.setUserName(userName);
        logEntity.setContent(content);
        logEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
        logEntity.setLevel(level == null ? LogLevel.INFO.getLevel() : logEntity.getLevel());
        dao.insert(logEntity);
    }

}
