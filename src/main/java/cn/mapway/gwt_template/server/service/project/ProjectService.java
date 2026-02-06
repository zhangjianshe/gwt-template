package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.git.GitRepoService;
import cn.mapway.gwt_template.shared.db.*;
import cn.mapway.gwt_template.shared.rpc.user.CommonPermission;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.impl.NutTxDao;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.List;

@Service
public class ProjectService {
    @Resource
    Dao dao;
    @Resource
    GitRepoService gitRepoService;

    public BizResult<DevProjectEntity> saveOrUpdateProject(String userName, DevProjectEntity project) {
        if (Strings.isBlank(project.getId())) {
            project.setId(R.UU16());
            project.setCreateTime(new Timestamp(
                    System.currentTimeMillis()
            ));
            NutTxDao txDao = new NutTxDao(dao);
            try {
                txDao.insert(project);
                //插入一个用户对这个项目拥有所有的权限
                DevMyProjectEntity devMyProjectEntity = new DevMyProjectEntity();
                devMyProjectEntity.projectId = project.getId();
                devMyProjectEntity.myId = project.getUserId();
                devMyProjectEntity.permission = CommonPermission.fromPermission(0).setAll().getPermission();
                txDao.insert(devMyProjectEntity);
                //创建这个用户的代码仓库
                gitRepoService.createNewRepository(userName, project.getName());

                return BizResult.success(project);
            } catch (Throwable e) {
                txDao.rollback();
                return BizResult.error(500, "创建项目错误" + e.getMessage());
            } finally {
                txDao.close();
            }
        } else {
            dao.updateIgnoreNull(project);
            project = dao.fetch(DevProjectEntity.class, project.getId());
            return BizResult.success(project);
        }
    }

    public List<VwProjectEntity> allProjects(Long userId) {
        if (userId == null) {
            return dao.query(VwProjectEntity.class, Cnd.orderBy().desc("create_time"));
        } else {
            return dao.query(VwProjectEntity.class, Cnd.where("my_id", "=", userId).desc("create_time"));
        }
    }

    public List<DevNodeEntity> allNodes() {
        return dao.query(DevNodeEntity.class, null);
    }

    public BizResult<DevNodeEntity> saveOrUpdateNode(DevNodeEntity node) {
        if (Strings.isBlank(node.getId())) {
            node.setId(R.UU16());
            node.setCreateTime(new Timestamp(
                    System.currentTimeMillis()
            ));
            dao.insert(node);
            return BizResult.success(node);
        } else {
            dao.updateIgnoreNull(node);
            node = dao.fetch(DevNodeEntity.class, node.getId());
            return BizResult.success(node);
        }
    }

    public CommonPermission userProjectPermission(Long userId, String projectId) {
        DevMyProjectEntity fetch = dao.fetch(DevMyProjectEntity.class, Cnd.where(
                DevMyProjectEntity.FLD_MYID, "=", userId
        ).and(DevMyProjectEntity.FLD_PROJECT_ID, "=", projectId));
        if (fetch == null) {
            return CommonPermission.fromPermission(0);
        } else {
            return CommonPermission.fromPermission(fetch.permission);
        }
    }

    public DevGroupEntity findGroupByName(String name) {
        return dao.fetch(DevGroupEntity.class, name);
    }

    public DevGroupMemberEntity findGroupMemberByMemberId(String name, Long userId) {
        Cnd where = Cnd.where(DevGroupMemberEntity.FLD_GROUP_NAME, "=", name)
                .and(DevGroupMemberEntity.FLD_USER_ID, "=", userId);
        return dao.fetch(DevGroupMemberEntity.class, where);
    }

    public void updateGroupMember(String groupName) {
        int count = dao.count(DevGroupMemberEntity.class, Cnd.where(DevGroupMemberEntity.FLD_GROUP_NAME, "=", groupName));
        DevGroupEntity group = new DevGroupEntity();
        group.setName(groupName);
        group.setMemberCount(count);
        dao.updateIgnoreNull(group);
    }

    public List<DevGroupEntity> userGroups(Long userId) {
        return dao.query(DevGroupEntity.class, Cnd.where(DevGroupEntity.FLD_USERID, "=", userId).desc("create_time"));
    }
}
