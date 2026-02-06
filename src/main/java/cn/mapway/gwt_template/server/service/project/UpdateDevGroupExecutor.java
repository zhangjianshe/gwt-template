package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevGroupEntity;
import cn.mapway.gwt_template.shared.db.DevGroupMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevGroupRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevGroupResponse;
import cn.mapway.gwt_template.shared.rpc.user.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.dao.impl.NutTxDao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;

/**
 * UpdateDevGroupExecutor
 * 创建或者更新开发组
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateDevGroupExecutor extends AbstractBizExecutor<UpdateDevGroupResponse, UpdateDevGroupRequest> {
    @Resource
    ProjectService projectService;
    @Resource
    Dao dao;

    @Override
    protected BizResult<UpdateDevGroupResponse> process(BizContext context, BizRequest<UpdateDevGroupRequest> bizParam) {
        UpdateDevGroupRequest request = bizParam.getData();
        log.info("UpdateDevGroupExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        DevGroupEntity devGroup = request.getDevGroup();
        assertNotNull(devGroup, "没有提供开发组信息(devGroup)");
        assertTrue(Strings.isNotBlank(devGroup.getName()), "没有提供分组名称");
        DevGroupEntity groupInDB = projectService.findGroupByName(devGroup.getName());
        if (groupInDB == null) {

            // 新建
            assertTrue(Strings.isNotBlank(devGroup.getFullName()), "没有提供分组全名");
            devGroup.setIcon("");
            devGroup.setUserId(user.getUser().getUserId());
            devGroup.setCreateTime(new Timestamp(System.currentTimeMillis()));
            devGroup.setMemberCount(1);

            DevGroupMemberEntity groupMember = new DevGroupMemberEntity();
            groupMember.setUserId(user.getUser().getUserId());
            groupMember.setGroupName(devGroup.getName());
            groupMember.setPermission(CommonPermission.fromPermission(0).setAll().getPermission());
            groupMember.setOwner(true);

            //事务创建 分组以及为分组添加管理人员
            NutTxDao txDao = new NutTxDao(dao);
            try {
                txDao.beginRC();
                txDao.insert(devGroup);
                txDao.insert(groupMember);
                txDao.commit();
            } catch (Throwable e) {
                txDao.rollback();
                log.debug("tx fail", e);
            } finally {
                txDao.close();
            }

        } else {
            //更新
            DevGroupMemberEntity member = projectService.findGroupMemberByMemberId(devGroup.getName(), user.getUser().getUserId());
            assertNotNull(member, "没有权限修改数据");
            CommonPermission permission = CommonPermission.fromPermission(member.getPermission());
            assertTrue(permission.isAdmin(), "没有权限修改数据");
            dao.updateIgnoreNull(devGroup);
        }
        devGroup = dao.fetch(DevGroupEntity.class, devGroup.getName());
        UpdateDevGroupResponse response = new UpdateDevGroupResponse();
        response.setDevGroup(devGroup);
        return BizResult.success(response);
    }
}
