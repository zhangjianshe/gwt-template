package cn.mapway.gwt_template.server.config.git;

import cn.mapway.gwt_template.server.config.AppConfig;
import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.shared.rpc.user.CommonPermission;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.common.AttributeRepository;
import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.apache.sshd.git.GitLocationResolver;
import org.apache.sshd.git.pack.GitPackCommandFactory;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
@Slf4j
public class GitSshConfig {

    public static final AttributeRepository.AttributeKey<RbacUserEntity> SSH_USER_NAME = new AttributeRepository.AttributeKey<>();
    // Define the pattern to capture the command and the quoted path
    private static final Pattern GIT_COMMAND_PATTERN = Pattern.compile("^(git\\-[\\w\\-]+)\\s+'?/?([^']+)'?$");
    @Resource
    ProjectService projectService; // We will use this for SSH key lookups

    @Bean
    public SshServer sshServer(AppConfig appConfig) throws IOException {
        // Check if EdDSA (for ed25519) is actually available to the JVM
        if (!SecurityUtils.isEDDSACurveSupported()) {
            log.error("EdDSA (Ed25519) support is missing! Please check your pom.xml dependencies.");
        }

        SshServer sshd = SshServer.setUpDefaultServer();

        // 1. Set Port (Avoid 22 if not running as root)
        sshd.setPort(appConfig.getSshPort());

        // 2. Set Host Key (The server's identity)

        Path certPath = new File(FileCustomUtils.concatPath(appConfig.getCertRoot(), "cangling.key")).toPath();
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(certPath));
        sshd.setPasswordAuthenticator(null);
        // 3. Configure Git Command Factory
        // This connects SSH commands (git-upload-pack) to your file system
        sshd.setCommandFactory(new GitPackCommandFactory(new GitLocationResolver() {
            @Override
            public Path resolveRootDirectory(String s, String[] strings, ServerSession serverSession, FileSystem fileSystem) throws IOException {
                return new File(appConfig.getRepoRoot()).toPath();
            }
        }) {
            @Override
            public Command createCommand(ChannelSession channel, String command) throws IOException {
                // 1. Get the username we stored during authentication
                RbacUserEntity user = channel.getSession().getAttribute(SSH_USER_NAME);

                // 1. Parse using the logic above
                Matcher matcher = GIT_COMMAND_PATTERN.matcher(command);
                if (!matcher.find()) {
                    return new Command() {
                        private OutputStream out;
                        private ExitCallback callback;

                        @Override
                        public void setOutputStream(OutputStream out) {
                            this.out = out;
                        }

                        @Override
                        public void setInputStream(InputStream inputStream) {

                        }

                        @Override
                        public void setErrorStream(OutputStream outputStream) {

                        }

                        @Override
                        public void setExitCallback(ExitCallback cb) {
                            this.callback = cb;
                        }

                        @Override
                        public void start(ChannelSession channel, Environment env) throws IOException {
                            callback.onExit(127, "Unknown command"); // 127 is standard for 'command not found'
                        }

                        @Override
                        public void destroy(ChannelSession channelSession) throws Exception {

                        }
                    };
                }

                String action = matcher.group(1);
                String fullPath = matcher.group(2);

                // 2. Determine if it's a push or pull
                boolean isPush = action.equals("git-receive-pack");

                // 3. Extract metadata (assume zhangjianshe/zjk3.git format)
                String[] segments = fullPath.replace(".git", "").split("/");
                String owner = segments[segments.length - 2];
                String project = segments[segments.length - 1];

                // 4. Check Database Permissions
                // Since we are in a non-Spring managed context usually, use SpringUtils or constructor injection
                CommonPermission perm = projectService.findUserPermissionInProjectByName(user.getUserId(), owner, project);

                if ((isPush && !perm.canWrite()) || (!isPush && !perm.canRead())) {
                    return new Command() {
                        private OutputStream out;
                        private ExitCallback callback;

                        @Override
                        public void setInputStream(InputStream in) {
                        }

                        @Override
                        public void setOutputStream(OutputStream out) {
                            this.out = out;
                        }

                        @Override
                        public void setErrorStream(OutputStream err) {
                        }

                        @Override
                        public void setExitCallback(ExitCallback callback) {
                            this.callback = callback;
                        }

                        @Override
                        public void start(ChannelSession channel, Environment env) throws IOException {
                            String errorMsg = "\r\n [ACCESS DENIED] \r\n" +
                                    " User: " + user.getUserName() + "\r\n" +
                                    " Path: " + fullPath + "\r\n" +
                                    " Reason: Insufficient permissions for " + action + "\r\n\r\n";

                            if (out != null) {
                                out.write(errorMsg.getBytes(StandardCharsets.UTF_8));
                                out.flush();
                            }

                            if (callback != null) {
                                // 1 indicates a general error status to the Git client
                                callback.onExit(1, "Access Denied");
                            }
                        }

                        @Override
                        public void destroy(ChannelSession channel) {
                        }
                    };
                }

                // 5. If authorized, let the default JGit factory handle the heavy lifting
                return super.createCommand(channel, command);
            }
        });

        // 4. Authentication: Public Key (The most important part)
        sshd.setPublickeyAuthenticator((username, incomingKey, session) -> {
            log.info("Checking SSH key for user: {}", username);
            // 1. Fetch the keys for this user from your database
            // Assuming your service returns a list of strings like "ssh-ed25519 AAA..."
            List<String> userKeysFromDb = projectService.getUserSshKeys(username);
            RbacUserEntity user = projectService.getUserEntity(username);
            if (user == null) {
                log.warn("[GIT SSH] User {} not exists", username);
                return false;
            }
            if (userKeysFromDb == null || userKeysFromDb.isEmpty()) {
                log.warn("No keys found in database for user: {}", username);
                return false;
            }

            for (String keyStr : userKeysFromDb) {
                try {
                    // Clean common copy-paste artifacts
                    String cleanKey = keyStr.trim().replaceAll("\\r|\\n", "");
                    AuthorizedKeyEntry entry = AuthorizedKeyEntry.parseAuthorizedKeyEntry(cleanKey);

                    // USE THIS METHOD - it is safer than appendPublicKey
                    PublicKey dbPublicKey = entry.resolvePublicKey(session, null, null);

                    if (dbPublicKey != null && KeyUtils.compareKeys(incomingKey, dbPublicKey)) {
                        log.info("Match found! Authenticating user: {}", username);
                        // Important for JGit to know who is performing the action
                        session.setAttribute(SSH_USER_NAME, user);
                        return true;
                    }
                } catch (Exception e) {
                    log.error("Error parsing key from database for user " + username, e);
                }
            }
            log.warn("No matching keys found for user: {}", username);
            return false;
        });

        sshd.start();
        log.info("Git SSH Server started on port 2222");
        return sshd;
    }

}