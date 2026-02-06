package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevGroupMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryGroupMemberRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryGroupMemberResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryGroupMemberExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryGroupMemberExecutor extends AbstractBizExecutor<QueryGroupMemberResponse, QueryGroupMemberRequest> {
    @Resource
    ProjectService projectService;
    @Resource
    Dao dao;

    @Override
    protected BizResult<QueryGroupMemberResponse> process(BizContext context, BizRequest<QueryGroupMemberRequest> bizParam) {
        QueryGroupMemberRequest request = bizParam.getData();
        log.info("QueryGroupMemberExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getGroupName()), "没有分组信息");

        DevGroupMemberEntity member = projectService.findGroupMemberByMemberId(request.getGroupName(), user.getUser().getUserId());
        assertNotNull(member, "您不在开发组中");

        List<DevGroupMemberEntity> query = dao.query(DevGroupMemberEntity.class, Cnd.where(DevGroupMemberEntity.FLD_GROUP_NAME, "=", request.getGroupName()).asc("create_time"));

        QueryGroupMemberResponse response = new QueryGroupMemberResponse();
        response.setMembers(query);
        return BizResult.success(response);
    }
}
