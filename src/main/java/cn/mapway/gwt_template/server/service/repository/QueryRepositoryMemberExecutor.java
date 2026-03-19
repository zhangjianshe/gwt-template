package cn.mapway.gwt_template.server.service.repository;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevRepositoryMemberEntity;
import cn.mapway.gwt_template.shared.db.VwRepositoryMemberEntity;
import cn.mapway.gwt_template.shared.rpc.repository.QueryRepositoryMemberRequest;
import cn.mapway.gwt_template.shared.rpc.repository.QueryRepositoryMemberResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryRepositoryMemberExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryRepositoryMemberExecutor extends AbstractBizExecutor<QueryRepositoryMemberResponse, QueryRepositoryMemberRequest> {
    @Resource
    RepositoryService repositoryService;
    @Override
    protected BizResult<QueryRepositoryMemberResponse> process(BizContext context, BizRequest<QueryRepositoryMemberRequest> bizParam) {
        QueryRepositoryMemberRequest request = bizParam.getData();
        log.info("QueryProjectMemberExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        DevRepositoryMemberEntity member = repositoryService.findProjectMemberByMemberId(request.getRepositoryId(), user.getUser().getUserId());
        assertNotNull(member, "您不在项目中");
        String currentUserPermission = member.getPermission();

        List<VwRepositoryMemberEntity> members= repositoryService.findProjectMembers(request.getRepositoryId());
        QueryRepositoryMemberResponse response = new QueryRepositoryMemberResponse();
        response.setMembers(members);
        response.setCurrentUserPermission(currentUserPermission);
        return BizResult.success(response);
    }
}
