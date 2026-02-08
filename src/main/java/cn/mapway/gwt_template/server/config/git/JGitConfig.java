package cn.mapway.gwt_template.server.config.git;

import cn.mapway.gwt_template.server.config.AppConfig;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.server.service.user.login.LoginProvider;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.util.FS;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
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
    public FilterRegistrationBean<GitAuthFilter> gitAuthFilterRegistration(LoginProvider loginProvider, ProjectService projectService) {
        FilterRegistrationBean<GitAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new GitAuthFilter(loginProvider, projectService));
        registration.addUrlPatterns("/code/*"); // Only protect git traffic
        registration.setName("gitAuthFilter");
        registration.setOrder(1); // Run before the GitServlet
        return registration;
    }

    @Bean
    public ServletRegistrationBean<GitServlet> gitServletRegistration(
            final ProjectService projectService) {
        GitServlet gitServlet = new GitServlet();

        // 1. The Repository Resolver tells JGit WHERE to find the .git folders
        // 1. Tell JGit where your repositories are stored
        gitServlet.setRepositoryResolver((request, name) -> {
            // 1. 确保基础路径存在
            File root = new File(appConfig.getRepoRoot());

            // 2. 尝试几种可能的路径组合
            // 路径 A: 原始路径 (如 zhangjianshe/zjk3.git)
            File repoDir = new File(root, name);

            // 路径 B: 如果 name 带有 .git 但文件夹没有，或者反之
            if (!repoDir.exists()) {
                if (name.endsWith(".git")) {
                    repoDir = new File(root, name.substring(0, name.length() - 4));
                } else {
                    repoDir = new File(root, name + ".git");
                }
            }

            System.out.println("最终物理路径: " + repoDir.getAbsolutePath());

            if (!repoDir.exists()) {
                throw new RepositoryNotFoundException(name);
            }
            try {
                Repository repository = RepositoryCache.open(
                        RepositoryCache.FileKey.exact(repoDir, FS.DETECTED), true
                );
                repository.getConfig().setBoolean("http", null, "receivepack", true);
                return repository;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


        //当PUSH发生的时候　执行此操作
        gitServlet.setReceivePackFactory((request, db) -> {
            ReceivePack rp = new ReceivePack(db);

            rp.setPostReceiveHook((receivePack, commands) -> {

                projectService.handlePostReceiveHook(rp,commands);

            });
            return rp;
        });

        // 2. Map the servlet to the /git/ path
        ServletRegistrationBean<GitServlet> reg = new ServletRegistrationBean<>(gitServlet, "/code/*");
        reg.addInitParameter("export-all", "true");
        // IMPORTANT: Set a high load-on-startup priority
        reg.setLoadOnStartup(1);
        // IMPORTANT: Ensure this servlet is checked before or instead of the DispatcherServlet
        reg.setOrder(Integer.MIN_VALUE);
        reg.setName("gitServlet");
        return reg;
    }
}