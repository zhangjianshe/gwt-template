package cn.mapway.gwt_template.server.config.git;

import cn.mapway.gwt_template.server.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

@Configuration
@Slf4j
public class JGitConfig {

    @Resource
    AppConfig appConfig;

    @Bean
    public ServletRegistrationBean<GitServlet> gitServletRegistration() {
        GitServlet gitServlet = new GitServlet();

        // 1. The Repository Resolver tells JGit WHERE to find the .git folders
        gitServlet.setRepositoryResolver((req, name) -> {
            // 'name' comes from the URL, e.g., "admin/my-project.git"
            File repoDir = new File(appConfig.getRepoRoot(), name);

            if (!repoDir.exists()) {
                String msg = "仓库" + name + "不存在";
                log.error("[GIT] {}", msg);
                throw new RepositoryNotFoundException(msg);
            }

            try {
                return FileRepositoryBuilder.create(repoDir);
            } catch (IOException e) {
                String msg = "仓库" + name + "打开错误" + e.getMessage();
                log.error("[GIT] {}", msg);
                throw new RepositoryNotFoundException(msg);
            }
        });

        // 2. Map the servlet to the /git/ path
        return new ServletRegistrationBean<GitServlet>(gitServlet, "/git/*");
    }
}