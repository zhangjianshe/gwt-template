package cn.mapway.gwt_template.server.service.desktop;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DesktopItemEntity;
import cn.mapway.gwt_template.shared.rpc.desktop.DeleteDesktopRequest;
import cn.mapway.gwt_template.shared.rpc.desktop.DeleteDesktopResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * DeleteDesktopExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteDesktopExecutor extends AbstractBizExecutor<DeleteDesktopResponse, DeleteDesktopRequest> {

    @Resource
    Dao dao;
    @Override
    protected BizResult<DeleteDesktopResponse> process(BizContext context, BizRequest<DeleteDesktopRequest> bizParam) {
        DeleteDesktopRequest request = bizParam.getData();
        log.info("DeleteDesktopExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getItemId()),"没有删除的项");
        DesktopItemEntity fetch = dao.fetch(DesktopItemEntity.class, request.getItemId());
        assertNotNull(fetch,"找不到删除的项"+request.getItemId());

        assertTrue(user.getUser().getUserId().equals(fetch.getUserId()),"没有权限删除");
        dao.delete(DesktopItemEntity.class, request.getItemId());
        return BizResult.success(new DeleteDesktopResponse());
    }
}
