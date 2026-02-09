package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.git.GitRepoService;
import cn.mapway.gwt_template.shared.db.*;
import cn.mapway.gwt_template.shared.rpc.user.CommonPermission;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.impl.NutTxDao;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
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

            DevProjectEntity fetch = dao.fetch(DevProjectEntity.class, Cnd.where(DevProjectEntity.FLD_OWNER_NAME, "=", userName)
                    .and(DevProjectEntity.FLD_NAME, "=", project.getName()));
            if (fetch != null) {
                return BizResult.error(500, project.getName() + "已经存在不能创建");
            }

            BizResult<String> newRepository = gitRepoService.createNewRepository(userName, project.getName());
            if (newRepository.isFailed()) {
                return newRepository.asBizResult();
            }

            NutTxDao txDao = new NutTxDao(dao);
            try {
                txDao.beginRC();
                txDao.insert(project);
                //插入一个用户对这个项目拥有所有的权限
                DevProjectMemberEntity devProjectMemberEntity = new DevProjectMemberEntity();
                devProjectMemberEntity.setProjectId(project.getId());
                devProjectMemberEntity.setUserId(project.getUserId());
                devProjectMemberEntity.setCreateTime(project.getCreateTime());
                devProjectMemberEntity.setOwner(true);
                devProjectMemberEntity.setPermission(CommonPermission.fromPermission(0).setAll().getPermission());

                txDao.insert(devProjectMemberEntity);
                //创建这个用户的代码仓库
                txDao.commit();
                project = dao.fetch(DevProjectEntity.class, project.getId());
                return BizResult.success(project);
            } catch (Throwable e) {
                e.printStackTrace();
                txDao.rollback();
                gitRepoService.deleteRepository(userName, project.getName());
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
        return dao.query(VwProjectEntity.class, Cnd.where("my_id", "=", userId).desc("create_time"));
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
        DevProjectMemberEntity fetch = dao.fetch(DevProjectMemberEntity.class, Cnd.where(
                DevProjectMemberEntity.FLD_USER_ID, "=", userId
        ).and(DevProjectMemberEntity.FLD_PROJECT_ID, "=", projectId));
        if (fetch == null) {
            return CommonPermission.fromPermission(0);
        } else {
            return CommonPermission.fromPermission(fetch.getPermission());
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

    /**
     * 获取用户 在项目中的权限
     *
     * @param userId
     * @param projectId
     * @return
     */
    public CommonPermission findUserPermissionInProject(Long userId, String projectId) {
        CommonPermission permission = CommonPermission.fromPermission(0);
        if (userId != null && Strings.isNotBlank(projectId)) {
            DevProjectMemberEntity devMyProject = dao.fetch(
                    DevProjectMemberEntity.class, Cnd.where(DevProjectMemberEntity.FLD_PROJECT_ID, "=", projectId)
                            .and(DevProjectMemberEntity.FLD_USER_ID, "=", userId)
            );
            if (devMyProject != null) {
                permission = CommonPermission.fromPermission(devMyProject.getPermission());
            }
        }
        return permission;
    }

    public DevProjectMemberEntity findProjectMemberByMemberId(String projectId, Long userId) {
        return dao.fetch(
                DevProjectMemberEntity.class, Cnd.where(DevProjectMemberEntity.FLD_PROJECT_ID, "=", projectId)
                        .and(DevProjectMemberEntity.FLD_USER_ID, "=", userId)
        );
    }

    public void updateProjectMember(String projectId) {

        int count = dao.count(DevProjectMemberEntity.class, Cnd.where(DevProjectMemberEntity.FLD_PROJECT_ID, "=", projectId));
        DevProjectEntity project = new DevProjectEntity();
        project.setId(projectId);
        project.setMemberCount(count);
        dao.updateIgnoreNull(project);
    }

    public List<VwProjectMemberEntity> findProjectMembers(String projectId) {
        return dao.query(VwProjectMemberEntity.class, Cnd.where(VwProjectMemberEntity.FLD_PROJECT_ID, "=", projectId).asc("create_time"));
    }

    public VwProjectMemberEntity findProjectMemberViewByMemberId(String projectId, Long userId) {
        return dao.fetch(
                VwProjectMemberEntity.class, Cnd.where(VwProjectMemberEntity.FLD_PROJECT_ID, "=", projectId)
                        .and(VwProjectMemberEntity.FLD_USER_ID, "=", userId)
        );
    }

    public DevProjectEntity findProjectById(String projectId) {
        return dao.fetch(DevProjectEntity.class, Cnd.where(DevProjectEntity.FLD_ID, "=", projectId));
    }

    public CommonPermission findUserPermissionInProjectByName(Long userId, String ownerName, String projectName) {
        CommonPermission permission = CommonPermission.fromPermission(0);
        DevProjectEntity project = findProjectByOwnerAndName(ownerName, projectName);
        if (project == null) {
            return permission;
        }
        if (userId != null) {
            DevProjectMemberEntity devMyProject = dao.fetch(
                    DevProjectMemberEntity.class, Cnd.where(DevProjectMemberEntity.FLD_PROJECT_ID, "=", project.getId())
                            .and(DevProjectMemberEntity.FLD_USER_ID, "=", userId)
            );
            if (devMyProject != null) {
                permission = CommonPermission.fromPermission(devMyProject.getPermission());
            }
        }
        return permission;
    }

    private DevProjectEntity findProjectByOwnerAndName(String ownerName, String projectName) {
        return dao.fetch(DevProjectEntity.class, Cnd.where(DevProjectEntity.FLD_OWNER_NAME, "=", ownerName)
                .and(DevProjectEntity.FLD_NAME, "=", projectName));
    }

    public boolean isProjectPublic(String ownerName, String projectName) {
        DevProjectEntity projectByOwnerAndName = findProjectByOwnerAndName(ownerName, projectName);
        return projectByOwnerAndName != null && projectByOwnerAndName.getIsPublic();
    }

    /**
     * 当用户PUSH到仓库后　会调用这个方法
     *
     * @param rp
     * @param commands
     */
    public void handlePostReceiveHook(ReceivePack rp, Collection<ReceiveCommand> commands) {
        for (ReceiveCommand cmd : commands) {
            if (cmd.getResult() == ReceiveCommand.Result.OK) {
                String newId = cmd.getNewId().name();
            }
        }
        log.info("[PROJECT GIT] handlePostReceiveHook finished");
    }

    public List<String> getUserSshKeys(String username) {
        RbacUserEntity fetch = dao.fetch(RbacUserEntity.class, Cnd.where(RbacUserEntity.FLD_USER_NAME, "=", username));
        if (fetch == null) {
            return Lang.list();
        }
        List<SysUserKeyEntity> keyEntities = dao.query(SysUserKeyEntity.class, Cnd.where(SysUserKeyEntity.FLD_USER_ID, "=", fetch.getUserId()));
        return keyEntities.stream().map(SysUserKeyEntity::getKey).collect(Collectors.toList());
    }

    public RbacUserEntity getUserEntity(String username) {
        RbacUserEntity fetch = dao.fetch(RbacUserEntity.class, Cnd.where(RbacUserEntity.FLD_USER_NAME, "=", username));
        return fetch;
    }

    public SysUserKeyEntity findPublicKeyById(String fingerPrint) {
        return dao.fetch(SysUserKeyEntity.class,fingerPrint);
    }
}
