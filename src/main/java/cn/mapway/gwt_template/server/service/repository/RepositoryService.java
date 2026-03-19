package cn.mapway.gwt_template.server.service.repository;

import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.git.GitPushPayload;
import cn.mapway.gwt_template.server.service.git.GitRepoService;
import cn.mapway.gwt_template.server.service.webhook.WebHookService;
import cn.mapway.gwt_template.shared.db.*;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.repository.RepositoryStatus;
import cn.mapway.gwt_template.shared.rpc.webhook.WebHookSourceKind;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.impl.NutTxDao;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RepositoryService {
    @Resource
    Dao dao;
    @Lazy
    @Resource
    GitRepoService gitRepoService;

    @Resource
    WebHookService webHookService;

    public BizResult<DevRepositoryEntity> saveOrUpdateProject(String userName, DevRepositoryEntity repository) {
        if (Strings.isBlank(repository.getId())) {
            repository.setId(R.UU16());
            repository.setCreateTime(new Timestamp(
                    System.currentTimeMillis()
            ));
            if (Strings.isBlank(repository.getFullName())) {
                repository.setFullName(repository.getName());
            }
            DevRepositoryEntity fetch = dao.fetch(DevRepositoryEntity.class, Cnd.where(DevRepositoryEntity.FLD_OWNER_NAME, "=", userName)
                    .and(DevRepositoryEntity.FLD_NAME, "=", repository.getName()));
            if (fetch != null) {
                return BizResult.error(500, repository.getName() + "已经存在不能创建");
            }

            BizResult<String> newRepository = gitRepoService.createNewRepository(userName, repository.getName());
            if (newRepository.isFailed()) {
                return newRepository.asBizResult();
            }
            repository.setStatus(RepositoryStatus.PS_INIT.getCode());

            NutTxDao txDao = new NutTxDao(dao);
            try {
                txDao.beginRC();
                txDao.insert(repository);
                //插入一个用户对这个项目拥有所有的权限
                DevRepositoryMemberEntity devRepositoryMemberEntity = new DevRepositoryMemberEntity();
                devRepositoryMemberEntity.setRepositoryId(repository.getId());
                devRepositoryMemberEntity.setUserId(repository.getUserId());
                devRepositoryMemberEntity.setCreateTime(repository.getCreateTime());
                devRepositoryMemberEntity.setOwner(true);
                devRepositoryMemberEntity.setPermission(CommonPermission.owner().toString());

                txDao.insert(devRepositoryMemberEntity);
                //创建这个用户的代码仓库
                txDao.commit();
                repository = dao.fetch(DevRepositoryEntity.class, repository.getId());
                return BizResult.success(repository);
            } catch (Throwable e) {
                e.printStackTrace();
                txDao.rollback();
                gitRepoService.deleteRepository(userName, repository.getName());
                return BizResult.error(500, "创建项目错误" + e.getMessage());
            } finally {
                txDao.close();
            }
        } else {
            repository.setStatus(null);//不更新状态
            dao.updateIgnoreNull(repository);
            repository = dao.fetch(DevRepositoryEntity.class, repository.getId());
            return BizResult.success(repository);
        }
    }

    public List<VwRepositoryEntity> allRepositories(Long userId, String nameFilter) {
        if (Strings.isBlank(nameFilter)) {
            return dao.query(VwRepositoryEntity.class, Cnd.where("my_id", "=", userId).desc("create_time"));
        } else {
            return dao.query(VwRepositoryEntity.class, Cnd.where("my_id", "=", userId).and("name", "like", "%" + nameFilter + "%").desc("create_time"));
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

    public CommonPermission userRepoPermission(Long userId, String repoId) {
        DevRepositoryMemberEntity fetch = dao.fetch(DevRepositoryMemberEntity.class, Cnd.where(
                DevRepositoryMemberEntity.FLD_USER_ID, "=", userId
        ).and(DevRepositoryMemberEntity.FLD_REPOSITORY_ID, "=", repoId));
        if (fetch == null) {
            return CommonPermission.empty();
        } else {
            return CommonPermission.from(fetch.getPermission());
        }
    }

    /**
     * 获取用户 在项目中的权限
     *
     * @param userId
     * @param repositoryId
     * @return
     */
    public CommonPermission findUserPermissionInRepository(Long userId, String repositoryId) {
        CommonPermission permission = CommonPermission.empty();
        if (userId != null && Strings.isNotBlank(repositoryId)) {
            DevRepositoryMemberEntity devMyProject = dao.fetch(
                    DevRepositoryMemberEntity.class, Cnd.where(DevRepositoryMemberEntity.FLD_REPOSITORY_ID, "=", repositoryId)
                            .and(DevRepositoryMemberEntity.FLD_USER_ID, "=", userId)
            );
            if (devMyProject != null) {
                permission = CommonPermission.from(devMyProject.getPermission());
            }
        }
        return permission;
    }

    public DevRepositoryMemberEntity findRepositoryMemberByMemberId(String projectId, Long userId) {
        return dao.fetch(
                DevRepositoryMemberEntity.class, Cnd.where(DevRepositoryMemberEntity.FLD_REPOSITORY_ID, "=", projectId)
                        .and(DevRepositoryMemberEntity.FLD_USER_ID, "=", userId)
        );
    }

    public void updateProjectMember(String projectId) {

        int count = dao.count(DevRepositoryMemberEntity.class, Cnd.where(DevRepositoryMemberEntity.FLD_REPOSITORY_ID, "=", projectId));
        DevRepositoryEntity project = new DevRepositoryEntity();
        project.setId(projectId);
        project.setMemberCount(count);
        dao.updateIgnoreNull(project);
    }

    public List<VwRepositoryMemberEntity> findProjectMembers(String projectId) {
        return dao.query(VwRepositoryMemberEntity.class, Cnd.where(VwRepositoryMemberEntity.FLD_REPOSITORY_ID, "=", projectId).asc("create_time"));
    }

    public VwRepositoryMemberEntity findProjectMemberViewByMemberId(String projectId, Long userId) {
        return dao.fetch(
                VwRepositoryMemberEntity.class, Cnd.where(VwRepositoryMemberEntity.FLD_REPOSITORY_ID, "=", projectId)
                        .and(VwRepositoryMemberEntity.FLD_USER_ID, "=", userId)
        );
    }

    public DevRepositoryEntity findProjectById(String projectId) {
        return dao.fetch(DevRepositoryEntity.class, Cnd.where(DevRepositoryEntity.FLD_ID, "=", projectId));
    }

    public CommonPermission findUserPermissionInRepoByName(Long userId, String ownerName, String projectName) {
        CommonPermission permission = CommonPermission.empty();
        DevRepositoryEntity project = findRepoByOwnerAndName(ownerName, projectName);
        if (project == null) {
            return permission;
        }
        if (userId != null) {
            DevRepositoryMemberEntity devMyProject = dao.fetch(
                    DevRepositoryMemberEntity.class, Cnd.where(DevRepositoryMemberEntity.FLD_REPOSITORY_ID, "=", project.getId())
                            .and(DevRepositoryMemberEntity.FLD_USER_ID, "=", userId)
            );
            if (devMyProject != null) {
                permission = CommonPermission.from(devMyProject.getPermission());
            }
        }
        return permission;
    }

    private DevRepositoryEntity findRepoByOwnerAndName(String ownerName, String projectName) {
        return dao.fetch(DevRepositoryEntity.class, Cnd.where(DevRepositoryEntity.FLD_OWNER_NAME, "=", ownerName)
                .and(DevRepositoryEntity.FLD_NAME, "=", projectName));
    }

    public boolean isRepoPublic(String ownerName, String projectName) {
        DevRepositoryEntity projectByOwnerAndName = findRepoByOwnerAndName(ownerName, projectName);
        return projectByOwnerAndName != null && projectByOwnerAndName.getIsPublic();
    }

    /**
     * 当用户PUSH到仓库后　会调用这个方法
     *
     * @param rp
     * @param commands
     */
    public void handlePostReceiveHook(ReceivePack rp, Collection<ReceiveCommand> commands) {
        // 1. Identify the repository
        DevRepositoryEntity project = findProjectByPath(rp.getRepository().getDirectory().getAbsolutePath());
        if (project == null) {
            log.error("[WEBHOOK] 不能找到项目 {}", rp.getRepository().getDirectory().getAbsolutePath());
            return;
        }

        // 2. Filter for successful commands only
        List<ReceiveCommand> successCommands = commands.stream()
                .filter(c -> c.getResult() == ReceiveCommand.Result.OK)
                .collect(Collectors.toList());

        if (successCommands.isEmpty()) return;

        // 3. Create a single "Bulk" Payload
        GitPushPayload payload = new GitPushPayload();
        payload.setProjectId(project.getId());
        payload.setProjectName(project.getName());
        payload.setOwnerName(project.getOwnerName());
        payload.setPushTime(new Timestamp(System.currentTimeMillis()));

        // Summarize the changes
        // If it's one command, we treat it normally. If multiple, we label it 'multi-ref update'
        if (successCommands.size() == 1) {
            ReceiveCommand cmd = successCommands.get(0);
            payload.setRef(cmd.getRefName());
            payload.setNewId(cmd.getNewId().name());
            payload.setEventType(getCommandType(cmd));
        } else {
            payload.setRef("multiple");
            payload.setEventType("bulk-update");
            payload.setNewId(successCommands.get(0).getNewId().name()); // Usually the main branch
        }

        try (RevWalk walk = new RevWalk(rp.getRepository())) {
            // Get the first successful command's new commit
            RevCommit commit = walk.parseCommit(successCommands.get(0).getNewId());
            payload.setMessage(commit.getShortMessage());
            payload.setAuthor(commit.getAuthorIdent().getName());
        } catch (Exception e) {
            log.error("Could not parse commit metadata", e);
        }

        // 4. Single Dispatch
        log.info("[GIT-HOOK] Single dispatch for repository: {}", project.getName());
        webHookService.triggerWebHooks(
                WebHookSourceKind.HOOK_SOURCE_REPOSITORY.getCode(),
                project.getId(),
                "push",
                payload
        );
    }

    private String getCommandType(ReceiveCommand cmd) {
        if (cmd.getOldId().equals(ObjectId.zeroId())) return "create";
        if (cmd.getNewId().equals(ObjectId.zeroId())) return "delete";
        return "update";
    }

    private DevRepositoryEntity findProjectByPath(String absolutePath) {
        // Standard format: .../ownerName/projectName.git
        // We need to normalize and extract the last two parts
        String normalized = absolutePath.replace("\\", "/");
        if (normalized.endsWith(".git")) {
            normalized = normalized.substring(0, normalized.length() - 4);
        }
        String[] parts = normalized.split("/");
        if (parts.length < 2) return null;

        String projectName = parts[parts.length - 1];
        String ownerName = parts[parts.length - 2];

        return findRepoByOwnerAndName(ownerName, projectName);
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
        return dao.fetch(SysUserKeyEntity.class, fingerPrint);
    }

    public void deleteWebhookInstance(String instanceId) {
        dao.delete(WebHookInstanceEntity.class, instanceId);
    }

    public VwRepositoryEntity findRepositoryView(String id) {
        return dao.fetch(VwRepositoryEntity.class, Cnd.where(VwRepositoryEntity.FLD_ID, "=", id));
    }
}
