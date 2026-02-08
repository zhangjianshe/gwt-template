package cn.mapway.gwt_template.server.config.git;

import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.server.service.user.login.LoginProvider;
import cn.mapway.gwt_template.shared.rpc.user.CommonPermission;
import cn.mapway.rbac.shared.rpc.LoginResponse;
import cn.mapway.ui.client.IUserInfo;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GitAuthFilter extends OncePerRequestFilter {

    LoginProvider loginProvider;
    ProjectService projectService;

    private final Cache<String, LoginResponse> authCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES) // Cache credentials for 5 mins
            .maximumSize(1000) // Don't let the cache grow too large
            .build();

    public GitAuthFilter(LoginProvider loginProvider,ProjectService projectService) {
        this.loginProvider = loginProvider;
        this.projectService = projectService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI(); // e.g., /code/zhangjianshe/zjk3.git/info/refs
        String contextPath = request.getContextPath();
        String pathWithinApp = uri.substring(contextPath.length()); // e.g., /code/zhangjianshe/zjk3.git/...

        // Assuming the format is always /code/{owner}/{project}.git/...
        String[] parts = pathWithinApp.split("/");
        if (parts.length < 4) {
            // Not a valid git request path for our structure
            filterChain.doFilter(request, response);
            return;
        }

        String ownerName = parts[2];
        String projectName = parts[3];

        // Clean up .git suffix if present
        if (projectName.endsWith(".git")) {
            projectName = projectName.substring(0, projectName.length() - 4);
        }

        boolean isPush = uri.contains("git-receive-pack") ||
                "git-receive-pack".equals(request.getParameter("service"));


        boolean isPublic = projectService.isProjectPublic(ownerName, projectName);

        // If it's a read operation on a public project, allow without auth
        if (!isPush && isPublic) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            // Trigger the password prompt in the Git client
            response.setHeader("WWW-Authenticate", "Basic realm=\"Canling Git\"");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Decode: "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==" -> "username:password"
        String base64Credentials = authHeader.substring(6);
        byte[] decoded = Base64.getDecoder().decode(base64Credentials);
        String credentials = new String(decoded, StandardCharsets.UTF_8);

        // 1. Check Cache First
        LoginResponse loginData = authCache.getIfPresent(credentials);

        if (loginData == null) {
            // 2. Cache Miss: Perform real login
            String[] values = credentials.split(":", 2);
            BizResult<LoginResponse> loginResult = loginProvider.login(values[0], values[1]);

            if (loginResult.isSuccess()) {
                loginData = loginResult.getData();
                authCache.put(credentials, loginData); // Put into cache
            } else {
                log.info("[GIT FILTER] login failed for user {}", values[0]);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials");
                return;
            }
        }

        // 3. Perform Permission Check (Permissions change more often than logins,
        // so we check these against the DB/Service every time)
        IUserInfo currentUser = loginData.getCurrentUser();
        Long userId = Long.parseLong(currentUser.getId());
        CommonPermission permission = projectService.findUserPermissionInProjectByName(userId, ownerName, projectName);

        if (isPush ? permission.canWrite() : permission.canRead()) {
            filterChain.doFilter(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
        }
    }
}
