package cn.mapway.gwt_template.server.service.desktop;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DesktopItemEntity;
import cn.mapway.gwt_template.shared.rpc.desktop.UpdateDesktopRequest;
import cn.mapway.gwt_template.shared.rpc.desktop.UpdateDesktopResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;

/**
 * UpdateDesktopExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateDesktopExecutor extends AbstractBizExecutor<UpdateDesktopResponse, UpdateDesktopRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<UpdateDesktopResponse> process(BizContext context, BizRequest<UpdateDesktopRequest> bizParam) {
        UpdateDesktopRequest request = bizParam.getData();
        log.info("UpdateDesktopExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        DesktopItemEntity item = request.getItem();
        assertNotNull(item, "没有数据");

        if (Strings.isBlank(item.getId())) {
            assertTrue(Strings.isNotBlank(item.getName()), "没有名称");
            item.setUserId(user.getUser().getUserId());
            item.setCreateTime(new Timestamp(System.currentTimeMillis()));
            item.setId(R.UU16());
            if (Strings.isBlank(item.getSummary())) {
                item.setSummary(item.getName());
            }
            if (!user.isAdmin()) {
                item.setShare(false);
            }
            item = dao.insert(item);
        } else {
            DesktopItemEntity itemInDb = dao.fetch(DesktopItemEntity.class, item.getId());
            assertNotNull(itemInDb, "没有数据可更新");
            assertTrue(user.getUser().getUserId().equals(itemInDb.getUserId()), "没有权限修改");
            if (!user.isAdmin()) {
                item.setShare(false);
            }
            dao.updateIgnoreNull(item);
            item = dao.fetch(DesktopItemEntity.class, item.getId());
        }
        UpdateDesktopResponse response = new UpdateDesktopResponse();
        response.setItem(item);
        return BizResult.success(response);
    }
}
