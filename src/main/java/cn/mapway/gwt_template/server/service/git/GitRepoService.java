package cn.mapway.gwt_template.server.service.git;

import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.config.AppConfig;
import cn.mapway.gwt_template.shared.rpc.project.QueryRepoRefsResponse;
import cn.mapway.gwt_template.shared.rpc.project.RepoItem;
import cn.mapway.gwt_template.shared.rpc.project.git.GitRef;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
            return BizResult.error(500, "名称不合法: 仅支持英文、数字、下划线和连字符");
        }

        // 2. 拼接路径: repoRoot/owner/repoName.git
        String finalRepoName = repoName.endsWith(".git") ? repoName : repoName + ".git";
        File ownerDir = new File(appConfig.getRepoRoot(), owner);
        File repoDir = new File(ownerDir, finalRepoName);

        if (repoDir.exists()) {
            return BizResult.error(500, "该用户下已存在同名仓库");
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
            return BizResult.error(500, "Git初始化失败");
        }
    }

    private boolean isValid(String name) {
        return name != null && REPO_NAME_PATTERN.matcher(name).matches();
    }

    /**
     * 删除仓库
     *
     * @param userName 拥有者
     * @param repoName 仓库名
     */
    public BizResult<Boolean> deleteRepository(String userName, String repoName) {
        // 1. 同样的合法性校验，防止路径穿越攻击
        if (!isValid(userName) || !isValid(repoName)) {
            return BizResult.error(500, "名称不合法");
        }

        String finalRepoName = repoName.endsWith(".git") ? repoName : repoName + ".git";
        File ownerDir = new File(appConfig.getRepoRoot(), userName);
        File repoDir = new File(ownerDir, finalRepoName);

        // 2. 检查是否存在
        if (!repoDir.exists()) {
            return BizResult.error(404, "仓库不存在");
        }

        // 3. 执行递归删除
        // FileSystemUtils.deleteRecursively 是 Spring 提供的高效工具类
        boolean success = FileSystemUtils.deleteRecursively(repoDir);

        if (success) {
            log.info("[GIT] 仓库删除成功: {}/{}", userName, finalRepoName);
            return BizResult.success(true);
        } else {
            log.error("[GIT] 仓库删除失败: {}/{}", userName, finalRepoName);
            return BizResult.error(500, "删除文件夹失败，请检查文件权限或引用");
        }
    }

    /**
     * 仓库文件和目录列表
     *
     * @throws Exception
     */
    public List<RepoItem> listFiles(String owner, String repoName, String relPath, String ref) throws Exception {
        List<RepoItem> files = new ArrayList<>();
        String finalRepoName = repoName.endsWith(".git") ? repoName : repoName + ".git";
        File repoDir = new File(appConfig.getRepoRoot(), owner + "/" + finalRepoName);

        try (Repository repository = FileRepositoryBuilder.create(repoDir)) {
            String revision = (ref == null || ref.isEmpty()) ? "HEAD" : ref;
            ObjectId head = repository.resolve(revision);
            if (head == null) return files;

            try (RevWalk revWalk = new RevWalk(repository);
                 TreeWalk treeWalk = new TreeWalk(repository);
                 Git git = new Git(repository)) {

                RevCommit headCommit = revWalk.parseCommit(head);
                treeWalk.addTree(headCommit.getTree());
                treeWalk.setRecursive(false);

                // --- NAVIGATION LOGIC ---
                if (relPath != null && !relPath.isEmpty() && !relPath.equals("/") && !relPath.equals(".")) {
                    // Ensure path doesn't have leading/trailing slashes for the filter
                    String normalizedPath = relPath.startsWith("/") ? relPath.substring(1) : relPath;
                    if (normalizedPath.endsWith("/"))
                        normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);

                    treeWalk.setFilter(PathFilter.create(normalizedPath));

                    boolean found = false;
                    while (treeWalk.next()) {
                        if (treeWalk.getPathString().equals(normalizedPath)) {
                            if (treeWalk.isSubtree()) {
                                treeWalk.enterSubtree();
                                treeWalk.setFilter(null); // IMPORTANT: Clear filter to see children
                                found = true;
                                break;
                            }
                        } else if (treeWalk.isSubtree()) {
                            // If we are on a parent of our target, enter it
                            treeWalk.enterSubtree();
                        }
                    }
                    if (!found) return files;
                }

                // Capture the target depth so we don't bleed back into the parent
                int targetDepth = treeWalk.getDepth();

                while (treeWalk.next()) {
                    // If we moved back up to the parent level, we are done
                    if (treeWalk.getDepth() < targetDepth) break;

                    // Only process items at exactly the target depth (the children)
                    if (treeWalk.getDepth() == targetDepth) {
                        String fullGitPath = treeWalk.getPathString();
                        RepoItem item = new RepoItem();

                        // Display just the filename
                        String fileName = fullGitPath.contains("/")
                                ? fullGitPath.substring(fullGitPath.lastIndexOf("/") + 1)
                                : fullGitPath;

                        item.setName(fileName);
                        item.setPath(fullGitPath);
                        item.setDir(treeWalk.isSubtree());

                        if (!treeWalk.isSubtree()) {
                            item.setSize(repository.open(treeWalk.getObjectId(0)).getSize());
                        }

                        // Get last commit info for THIS specific path
                        Iterable<RevCommit> logs = git.log()
                                .add(head).addPath(fullGitPath).setMaxCount(1).call();
                        for (RevCommit lastCommit : logs) {
                            item.setSummary(lastCommit.getShortMessage());
                            item.setDate(new Date(lastCommit.getCommitTime() * 1000L));
                            item.setAuthor(lastCommit.getAuthorIdent().getName());
                        }

                        files.add(item);
                    }
                }
            }
        }
        return files;
    }

    /**
     * 获取文件内容
     */
    public BizResult<String> getFileContent(String owner, String repoName, String path) {
        String finalRepoName = repoName.endsWith(".git") ? repoName : repoName + ".git";
        File repoDir = new File(appConfig.getRepoRoot(), owner + "/" + finalRepoName);

        try (Repository repository = FileRepositoryBuilder.create(repoDir)) {
            ObjectId head = repository.resolve("HEAD");
            if (head == null) return BizResult.error(404, "空仓库");

            try (RevWalk revWalk = new RevWalk(repository);
                 TreeWalk treeWalk = new TreeWalk(repository)) {

                RevCommit commit = revWalk.parseCommit(head);
                treeWalk.addTree(commit.getTree());
                treeWalk.setRecursive(true); // 递归查找
                treeWalk.setFilter(PathFilter.create(path)); // 只找这个路径

                if (!treeWalk.next()) {
                    return BizResult.error(404, "文件不存在");
                }

                // 获取文件的 ObjectId
                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = repository.open(objectId);
                long size = loader.getSize();

                if (size > 1024 * 1024 * 2) { // 如果超过 2MB
                    return BizResult.error(500, "文件太大，请下载后查看");
                }

                // 将二进制内容转为字符串
                return BizResult.success(new String(loader.getBytes(), StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            log.error("[GIT] 读取文件内容错误: {}", e.getMessage());
            return BizResult.error(500, "读取失败");
        }
    }

    public void writeFileContentToStream(String owner, String repoName, String path, OutputStream out) {
        String finalRepoName = repoName.endsWith(".git") ? repoName : repoName + ".git";
        File repoDir = new File(appConfig.getRepoRoot(), owner + "/" + finalRepoName);

        try (Repository repository = FileRepositoryBuilder.create(repoDir)) {
            ObjectId head = repository.resolve("HEAD");
            if (head == null) return;

            try (RevWalk revWalk = new RevWalk(repository);
                 TreeWalk treeWalk = new TreeWalk(repository)) {

                RevCommit commit = revWalk.parseCommit(head);
                treeWalk.addTree(commit.getTree());
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathFilter.create(path));

                if (treeWalk.next()) {
                    ObjectId objectId = treeWalk.getObjectId(0);
                    ObjectLoader loader = repository.open(objectId);

                    // Use loader.copyTo(out) for efficient memory usage
                    loader.copyTo(out);
                    out.flush();
                }
            }
        } catch (Exception e) {
            log.error("[GIT] Raw content streaming error: {}", e.getMessage());
        }
    }


    public BizResult<QueryRepoRefsResponse> getRepoRefs(String owner, String repoName) {
        String finalRepoName = repoName.endsWith(".git") ? repoName : repoName + ".git";
        File repoDir = new File(appConfig.getRepoRoot(), owner + "/" + finalRepoName);
        List<GitRef> refs = new ArrayList<>();

        try (Repository repository = FileRepositoryBuilder.create(repoDir);
             Git git = new Git(repository)) {
            String defaultBranch = repository.getBranch();
            List<Ref> call = git.branchList().call();
            for (Ref ref : call) {
                GitRef gitRef = GitRef.branch(Repository.shortenRefName(ref.getName()));
                refs.add(gitRef);
            }
            call = git.tagList().call();
            for (Ref ref : call) {
                GitRef gitRef = GitRef.tag(Repository.shortenRefName(ref.getName()));
                refs.add(gitRef);
            }
            QueryRepoRefsResponse response = new QueryRepoRefsResponse();
            response.setRefs(refs);
            response.setDefaultBranch(defaultBranch);
            return BizResult.success(response);
        } catch (Exception e) {
            return BizResult.error(500, e.getMessage());
        }
    }
}