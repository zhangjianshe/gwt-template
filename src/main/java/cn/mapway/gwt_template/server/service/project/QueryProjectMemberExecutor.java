package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectMemberEntity;
import cn.mapway.gwt_template.shared.db.VwProjectMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectMemberRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectMemberResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryProjectMemberExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryProjectMemberExecutor extends AbstractBizExecutor<QueryProjectMemberResponse, QueryProjectMemberRequest> {
    @Resource
    ProjectService projectService;
    @Resource
    Dao dao;
    @Override
    protected BizResult<QueryProjectMemberResponse> process(BizContext context, BizRequest<QueryProjectMemberRequest> bizParam) {
        QueryProjectMemberRequest request = bizParam.getData();
        log.info("QueryProjectMemberExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        DevProjectMemberEntity member = projectService.findProjectMemberByMemberId(request.getProjectId(), user.getUser().getUserId());
        assertNotNull(member, "您不在项目中");
        Integer currentUserPermission = member.getPermission();

        List<VwProjectMemberEntity> members= projectService.findProjectMembers(request.getProjectId());
        QueryProjectMemberResponse response = new QueryProjectMemberResponse();
        response.setMembers(members);
        response.setCurrentUserPermission(currentUserPermission);
        return BizResult.success(response);
    }
}
