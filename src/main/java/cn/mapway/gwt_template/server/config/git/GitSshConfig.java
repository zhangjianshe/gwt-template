package cn.mapway.gwt_template.server.config.git;

import cn.mapway.gwt_template.server.config.AppConfig;
import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.shared.db.SysUserKeyEntity;
import cn.mapway.gwt_template.shared.rpc.user.CommonPermission;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.common.AttributeRepository;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.apache.sshd.core.CoreModuleProperties;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
@Slf4j
public class GitSshConfig {

    public static final AttributeRepository.AttributeKey<SysUserKeyEntity> SSH_USER_PUBLIC_KEY = new AttributeRepository.AttributeKey<>();
    // Define the pattern to capture the command and the quoted path
    private static final Pattern GIT_COMMAND_PATTERN = Pattern.compile("^(git-[\\w-]+)\\s+['\"]?/?(.*?)(.git)?['\"]?$");
    String BOLD_CYAN = "\u001b[1;36m";
    String RESET = "\u001b[0m";
    @Resource
    ProjectService projectService; // We will use this for SSH key lookups

    @Bean
    public SshServer sshServer(AppConfig appConfig) throws IOException {
        // Check if EdDSA (for ed25519) is actually available to the JVM
        if (!SecurityUtils.isEDDSACurveSupported()) {
            log.error("EdDSA (Ed25519) support is missing! Please check your pom.xml dependencies.");
        }

        SshServer sshd = SshServer.setUpDefaultServer();

        // Ensure the charset is explicitly UTF-8
        CoreModuleProperties.WELCOME_BANNER_CHARSET.set(sshd, StandardCharsets.UTF_8);

        // If your version supports it, this property prevents the server from
        // "cleaning" the string before sending
        sshd.getProperties().put("welcome-banner-language", "en");


        String satelliteBanner =
                "\r\n" +
                        "    [ CANGLING AI : ORBITAL DATA HUB ]\r\n" +
                        "    Remote Sensing - Satellite Imagery - AI\r\n" +
                        "    ---------------------------------------\r\n";
        CoreModuleProperties.WELCOME_BANNER.set(sshd, satelliteBanner);

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
                SysUserKeyEntity userPublicKey = channel.getSession().getAttribute(SSH_USER_PUBLIC_KEY);

                Matcher matcher = GIT_COMMAND_PATTERN.matcher(command.trim());
                if (!matcher.find()) {
                    return createErrorCommand("Unknown command: " + command, 127);
                }

                String action = matcher.group(1); // git-upload-pack or git-receive-pack
                String rawPath = matcher.group(2); // zhangjianshe/zjk3

                // Clean up the path: remove leading slashes and any trailing quotes/whitespace
                String cleanPath = rawPath.replaceAll("^/+", "").replaceAll("['\"\\s]+$", "");

                boolean isPush = action.equals("git-receive-pack");

                // Split segments: owner/project
                String[] segments = cleanPath.split("/");
                if (segments.length < 2) {
                    log.error("Invalid path format. Raw: {}, Clean: {}", rawPath, cleanPath);
                    return createErrorCommand("Invalid repository path: " + rawPath, 1);
                }

                String owner = segments[segments.length - 2];
                String project = segments[segments.length - 1];

                log.info("Auth Check: User {} requested {} on project {}/{}",
                        userPublicKey.getUserName(), action, owner, project);

                CommonPermission perm = projectService.findUserPermissionInProjectByName(
                        userPublicKey.getUserId(), owner, project);

                if ((isPush && !perm.canWrite()) || (!isPush && !perm.canRead())) {
                    String reason = "User " + userPublicKey.getUserName() + " (ID: " + userPublicKey.getUserId() +
                            ") has no " + (isPush ? "WRITE" : "READ") + " access to " + owner + "/" + project;
                    return createErrorCommand("[ACCESS DENIED] " + reason, 1);
                }

                return super.createCommand(channel, command);
            }
        });

        // 4. Authentication: Public Key (The most important part)
        sshd.setPublickeyAuthenticator((username, incomingKey, session) -> {
            // Standard SSH fingerprint (SHA256:...)
            String fingerPrint = KeyUtils.getFingerPrint(incomingKey);

            // Direct DB lookup by Primary Key (Fingerprint)
            SysUserKeyEntity keyInDb = projectService.findPublicKeyById(fingerPrint);

            if (keyInDb == null) {
                log.warn("[GIT SSH] Unauthorized key fingerprint: {}", fingerPrint);
                return false;
            }

            // Optional: check if the username in the SSH URL matches the key owner
            // If you want to force zhangjianshe to only use zhangjianshe's keys:
            if (!"git".equals(username) && !keyInDb.getUserName().equals(username)) {
                log.warn("[GIT SSH] Key owner {} does not match login user {}", keyInDb.getUserName(), username);
                return false;
            }

            session.setAttribute(SSH_USER_PUBLIC_KEY, keyInDb);
            return true;
        });


        sshd.setShellFactory(channel -> new Command() {
            private OutputStream out;
            private ExitCallback callback;

            @Override public void setOutputStream(OutputStream out) { this.out = out; }
            @Override public void setExitCallback(ExitCallback cb) { this.callback = cb; }
            @Override public void setInputStream(InputStream in) {}
            @Override public void setErrorStream(OutputStream err) {}

            @Override
            public void start(ChannelSession channel, Environment env) throws IOException {
                SysUserKeyEntity user = channel.getSession().getAttribute(SSH_USER_PUBLIC_KEY);

                // High-tech Terminal Colors
                String BOLD_CYAN = "\u001b[1;36m";
                String GREEN = "\u001b[1;32m";
                String RESET = "\u001b[0m";

                StringBuilder msg = new StringBuilder();
                msg.append("\r\n").append(BOLD_CYAN).append("  [ LINK ESTABLISHED : CANGLING-SAT-1 ]").append(RESET).append("\r\n");
                msg.append("  --------------------------------------------------\r\n");
                msg.append("  Operator:   ").append(BOLD_CYAN).append(user.getUserName()).append(RESET).append("\r\n");
                msg.append("  Channel:    Secure Orbital Link (SSH)\r\n");
                msg.append("  Status:     ").append(GREEN).append("READY - DATA STREAM ACTIVE").append(RESET).append("\r\n");
                msg.append("  AI Engine:  Cangling-v3 (Geospatial Optimized)\r\n");
                msg.append("  --------------------------------------------------\r\n");
                msg.append("  SYSTEM NOTE: Terminal access restricted.\r\n");
                msg.append("  Please use 'git push/pull' for satellite data sync.\r\n\r\n");

                if (out != null) {
                    out.write(msg.toString().getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }
                if (callback != null) {
                    callback.onExit(0);
                }
            }

            @Override public void destroy(ChannelSession channel) {}
        });

        sshd.start();
        log.info("Git SSH Server started on port 2222");
        return sshd;
    }

    private Command createErrorCommand(String message, int exitCode) {
        return new Command() {
            private ExitCallback callback;
            private OutputStream err;

            @Override
            public void setInputStream(InputStream in) {
            }

            // Use ErrorStream for human messages!
            @Override
            public void setErrorStream(OutputStream err) {
                this.err = err;
            }

            @Override
            public void setOutputStream(OutputStream out) {
            }

            @Override
            public void setExitCallback(ExitCallback callback) {
                this.callback = callback;
            }

            @Override
            public void start(ChannelSession channel, Environment env) throws IOException {
                if (err != null) {
                    String syncMsg = "remote: " + BOLD_CYAN + ">> Cangling AI: Synchronizing Orbital Data..." + RESET + "\r\n";
                    err.write(syncMsg.getBytes(StandardCharsets.UTF_8));
                    err.flush();
                }
                if (callback != null) {
                    callback.onExit(exitCode, message);
                }
            }

            @Override
            public void destroy(ChannelSession channel) {
            }
        };
    }
}