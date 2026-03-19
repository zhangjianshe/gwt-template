package cn.mapway.gwt_template.server.service.project.res;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectResourceMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.project.module.ResourceMember;
import cn.mapway.gwt_template.shared.rpc.project.res.QueryResourceMemberRequest;
import cn.mapway.gwt_template.shared.rpc.project.res.QueryResourceMemberResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * QueryResourceMemberExecutor
 * 查询项目资源的成员列表，合并用户资料与权限位图
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryResourceMemberExecutor extends AbstractBizExecutor<QueryResourceMemberResponse, QueryResourceMemberRequest> {

    @Resource
    private ProjectService projectService;

    @Resource
    private Dao dao;

    @Override
    protected BizResult<QueryResourceMemberResponse> process(BizContext context, BizRequest<QueryResourceMemberRequest> bizParam) {
        QueryResourceMemberRequest request = bizParam.getData();
        log.info("QueryResourceMemberExecutor {}", Json.toJson(request, JsonFormat.compact()));

        LoginUser loginUser = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        // 1. 参数合法性校验
        if (request == null || Strings.isBlank(request.getResourceId())) {
            return BizResult.error(500, "未指定资源ID");
        }

        // 2. 权限校验：调用 ProjectService 判定当前操作者是否有权限查看该资源的成员
        // 通常只需要拥有 READ 权限即可查看成员列表
        CommonPermission resourceMemberPermission = projectService.findUserPermissionInProjectResource(
                loginUser.getUser().getUserId(),
                request.getResourceId()
        );
        assertTrue(resourceMemberPermission.isSuper(), "您没有查看该资源成员列表的权限");


        // 3. 执行查询：获取所有成员记录
        List<DevProjectResourceMemberEntity> memberEntities = dao.query(DevProjectResourceMemberEntity.class,
                Cnd.where(DevProjectResourceMemberEntity.FLD_RESOURCE_ID, "=", request.getResourceId())
                        .asc(DevProjectResourceMemberEntity.FLD_CREATE_TIME)); // 按加入时间排序

        List<ResourceMember> members = new ArrayList<>();

        // 5. 返回结果
        QueryResourceMemberResponse response = new QueryResourceMemberResponse();
        // 4. 数据聚合：关联用户信息并转换为 ResourceMember 传输对象
        for (DevProjectResourceMemberEntity entity : memberEntities) {
            ResourceMember member = new ResourceMember();

            // 填充成员权限资料
            member.setResourceId(entity.getResourceId());
            member.setUserId(entity.getUserId());
            member.setPermission(entity.getPermission()); // 权限位图字符串 "1100..."
            member.setCreateTime(entity.getCreateTime());

            // 填充用户基础资料
            RbacUserEntity userEntity = dao.fetch(RbacUserEntity.class, entity.getUserId());
            if (userEntity != null) {
                member.setUserName(userEntity.getUserName()); // 登录账号
                member.setNickName(userEntity.getNickName());    // 显示昵称
                member.setAvatar(userEntity.getAvatar());    // 头像
                member.setEmail(userEntity.getEmail());      // 邮箱
            }

            members.add(member);
            if (loginUser.getUser().getUserId().equals(entity.getUserId())) {
                response.setCurrentPermission(entity.getPermission());
            }
        }


        response.setMembers(members);

        return BizResult.success(response);
    }
}