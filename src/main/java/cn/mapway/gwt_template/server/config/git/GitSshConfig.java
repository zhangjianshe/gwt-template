package cn.mapway.gwt_template.server.config.git;

import cn.mapway.gwt_template.server.config.AppConfig;
import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.server.service.user.login.LoginProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.git.GitLocationResolver;
import org.apache.sshd.git.pack.GitPackCommandFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.security.PublicKey;

@Configuration
@Slf4j
public class GitSshConfig {

    @Resource
    LoginProvider loginProvider; // We will use this for SSH key lookups

    @Bean
    public SshServer sshServer(AppConfig appConfig) throws IOException {
        SshServer sshd = SshServer.setUpDefaultServer();

        // 1. Set Port (Avoid 22 if not running as root)
        sshd.setPort(appConfig.getSshPort());

        // 2. Set Host Key (The server's identity)

        Path certPath=new File(FileCustomUtils.concatPath(appConfig.getCertRoot(),"cangling.key")).toPath();
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(certPath));

        // 3. Configure Git Command Factory
        // This connects SSH commands (git-upload-pack) to your file system
        GitPackCommandFactory commandFactory = new GitPackCommandFactory(new GitLocationResolver() {
            @Override
            public Path resolveRootDirectory(String s, String[] strings, ServerSession serverSession, FileSystem fileSystem) throws IOException {
                return Path.of(appConfig.getRepoRoot());
            }
        });
        sshd.setCommandFactory(commandFactory);

        // 4. Authentication: Public Key (The most important part)
        sshd.setPublickeyAuthenticator((username, key, session) -> {
            log.info("SSH Login attempt: {} with key type {}", username, key.getAlgorithm());
            // TODO: In your DB, find the user 'username' and check if 'key'
            // matches any of their registered public keys.
            return authenticateSshKey(username, key);
        });

        sshd.start();
        log.info("Git SSH Server started on port 2222");
        return sshd;
    }

    private boolean authenticateSshKey(String username, PublicKey key) {
        // You'll need a service to find keys: projectService.findKeysByUser(username)
        // Convert the PublicKey to a string (OpenSSH format) to compare with your DB
        return true; // For initial testing only!
    }
}