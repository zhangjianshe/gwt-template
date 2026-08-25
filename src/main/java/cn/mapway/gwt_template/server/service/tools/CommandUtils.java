package cn.mapway.gwt_template.server.service.tools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CommandUtils {

    @Data
    public static class ExecResult {
        private int exitCode;
        private String stdout;
        private String stderr;

        public boolean isSuccess() {
            return exitCode == 0;
        }
    }

    /**
     * 安全执行命令行指令
     *
     * @param command   命令及其参数列表，例如 List.of("docker-compose", "-f", "/path/docker-compose.yml", "ps")
     * @param workDir   工作目录（可为 null）
     * @param timeoutSeconds 超时时间（秒）
     */
    public static ExecResult exec(List<String> command, File workDir, long timeoutSeconds) {
        ExecResult result = new ExecResult();
        StringBuilder stdoutSb = new StringBuilder();
        StringBuilder stderrSb = new StringBuilder();

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            if (workDir != null && workDir.exists()) {
                pb.directory(workDir);
            }

            Process process = pb.start();

            // 异步读取 stdout
            Thread stdoutThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stdoutSb.append(line).append("\n");
                    }
                } catch (Exception e) {
                    log.error("Read stdout error", e);
                }
            });

            // 异步读取 stderr
            Thread stderrThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stderrSb.append(line).append("\n");
                    }
                } catch (Exception e) {
                    log.error("Read stderr error", e);
                }
            });

            stdoutThread.start();
            stderrThread.start();

            // 等待进程结束（支持超时控制）
            boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                result.setExitCode(-1);
                result.setStderr("Command timed out after " + timeoutSeconds + " seconds");
                return result;
            }

            stdoutThread.join();
            stderrThread.join();

            result.setExitCode(process.exitValue());
            result.setStdout(stdoutSb.toString().trim());
            result.setStderr(stderrSb.toString().trim());

        } catch (Exception e) {
            log.error("Exec command failed: {}", command, e);
            result.setExitCode(-1);
            result.setStderr("Execution error: " + e.getMessage());
        }

        return result;
    }
}
