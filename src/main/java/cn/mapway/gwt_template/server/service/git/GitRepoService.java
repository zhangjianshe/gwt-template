package cn.mapway.gwt_template.server.service.git;

import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.regex.Pattern;

@Slf4j
@Service
public class GitRepoService {

    // Define the allowed pattern: Alphanumeric, underscores, and hyphens only
    private static final Pattern REPO_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");
    @Resource
    AppConfig appConfig;

    /**
     * 创建带命名空间的仓库
     *
     * @param owner    拥有者(用户名或组织名)
     * @param repoName 仓库名
     */
    public BizResult<String> createNewRepository(String owner, String repoName) {
        // 1. 校验 Owner 和 RepoName 是否合法
        if (!isValid(owner) || !isValid(repoName)) {
            return BizResult.error(500,"名称不合法: 仅支持英文、数字、下划线和连字符");
        }

        // 2. 拼接路径: repoRoot/owner/repoName.git
        String finalRepoName = repoName.endsWith(".git") ? repoName : repoName + ".git";
        File ownerDir = new File(appConfig.getRepoRoot(), owner);
        File repoDir = new File(ownerDir, finalRepoName);

        if (repoDir.exists()) {
            return BizResult.error(500,"该用户下已存在同名仓库");
        }

        // 3. 自动创建父目录 (owner 文件夹)
        if (!ownerDir.exists()) {
            ownerDir.mkdirs();
        }

        try (Git git = Git.init()
                .setDirectory(repoDir)
                .setBare(true)
                .call()) {
            log.info("[GIT] 仓库创建成功: {}/{}", owner, finalRepoName);
            return BizResult.success(owner + "/" + finalRepoName);
        } catch (GitAPIException e) {
            log.error("[GIT] 创建失败: {}", e.getMessage());
            return BizResult.error(500,"Git初始化失败");
        }
    }

    private boolean isValid(String name) {
        return name != null && REPO_NAME_PATTERN.matcher(name).matches();
    }
}