package cn.mapway.gwt_template.server.service.docker;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DockerAppEntity;
import cn.mapway.gwt_template.shared.rpc.docker.DockerAppAction;
import cn.mapway.gwt_template.shared.rpc.docker.RestartDockerAppRequest;
import cn.mapway.gwt_template.shared.rpc.docker.RestartDockerAppResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * RestartDockerAppExecutor
 * 负责 Docker 应用整体或单个 Service 的启动、停止、重启操作
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class RestartDockerAppExecutor extends AbstractBizExecutor<RestartDockerAppResponse, RestartDockerAppRequest> {

    @Resource
    DockerAppService dockerAppService;

    @Override
    protected BizResult<RestartDockerAppResponse> process(BizContext context, BizRequest<RestartDockerAppRequest> bizParam) {
        RestartDockerAppRequest request = bizParam.getData();
        log.info("RestartDockerAppExecutor {}", Json.toJson(request, JsonFormat.compact()));

        // 1. 登录与权限校验
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(dockerAppService.canOperate(user), "无权限操作 Docker 应用");

        // 2. 查询应用实体
        DockerAppEntity appEntity = dockerAppService.findApp(request.getDockerAppId());
        assertNotNull(appEntity, "找不到对应的 Docker 应用");

        // 3. 确定操作类型 (DAA_SHUTDOWN, DAA_RESTART, DAA_START 等)
        DockerAppAction appAction = DockerAppAction.fromCode(request.getAction());
        assertNotNull(appAction, "无效的操作类型");

        String serviceName = request.getServiceName();

        // 4. 执行 docker compose 命令
        return executeDockerComposeCommand(appEntity, appAction, serviceName);
    }

    /**
     * 构造并执行 docker compose 命令
     *
     * @param appEntity   应用实体
     * @param action      操作枚举 (DAA_SHUTDOWN, DAA_RESTART, DAA_START)
     * @param serviceName 服务名称（为 Blank 时代表对整个 Compose 应用组操作）
     */
    private BizResult<RestartDockerAppResponse> executeDockerComposeCommand(DockerAppEntity appEntity, DockerAppAction action, String serviceName) {
        String composeFilePath = appEntity.getAbsolutePath();
        File composeFile = new File(composeFilePath);
        assertTrue(composeFile.exists(), "Docker Compose 配置文件不存在: " + composeFilePath);

        // 【核心修复】：如果是目录，自动寻找该目录下的 docker-compose.yml 或 docker-compose.yaml
        if (composeFile.isDirectory()) {
            File yamlFile = new File(composeFile, "docker-compose.yml");
            if (!yamlFile.exists()) {
                yamlFile = new File(composeFile, "docker-compose.yaml");
            }
            composeFile = yamlFile;
        }

        // 校验目标 YAML 文件是否存在且为文件
        assertTrue(composeFile.exists() && composeFile.isFile(), "Docker Compose 配置文件不存在或不是标准文件: " + composeFile.getAbsolutePath());

        boolean hasService = Strings.isNotBlank(serviceName);
        String targetService = hasService ? serviceName.trim() : "";

        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("compose");
        command.add("-f");
        command.add(composeFile.getAbsolutePath()); // 这里保证传给 -f 的必然是具体文件路径

        // 根据 Action 和 是否有指定 Service 严谨构造命令
        switch (action) {
            case DAA_SHUTDOWN:
                // 关键点修复：stop 兼容单服务与整体；down 仅支持整体销毁
                if (hasService) {
                    command.add("stop");
                    command.add(targetService);
                } else {
                    command.add("down");
                }
                break;

            case DAA_START:
                command.add("up");
                command.add("-d");
                if (hasService) {
                    command.add(targetService);
                }
                break;

            case DAA_RESTART:
            default:
                // 强制硬重启，不管是单服务还是整个 App 组均能完美适用
                command.add("restart");
                if (hasService) {
                    command.add(targetService);
                }
                break;
        }

        log.info("[DockerAction] 正在执行命令: {}", String.join(" ", command));

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(composeFile.getParentFile()); // 统一设置为 docker-compose.yml 所在目录
            pb.redirectErrorStream(true); // 合并 stderr 到 stdout

            Process process = pb.start();

            // 实时读取控制台日志输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // 设置 60 秒超时，防止某些容器停机过慢挂起线程
            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return fail("执行 Docker 命令超时，已被强制中断");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("[DockerAction] 命令执行失败 (code {}): \n{}", exitCode, output);
                return fail("Docker 操作失败: " + output);
            } else {
                log.info("[DockerAction] 命令执行成功: \n{}", output);
            }

            RestartDockerAppResponse response = new RestartDockerAppResponse();
            return BizResult.success(response);

        } catch (Exception e) {
            log.error("[DockerAction] 执行命令发生系统异常: ", e);
            return fail("执行 Docker 命令发生系统异常: " + e.getMessage());
        }
    }

    private BizResult<RestartDockerAppResponse> fail(String s) {
        return BizResult.error(500, s);
    }
}