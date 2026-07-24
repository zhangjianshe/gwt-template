package cn.mapway.gwt_template.server.service.docker;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.tools.CommandUtils;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DockerAppEntity;
import cn.mapway.gwt_template.shared.rpc.docker.QueryDockerAppInfoRequest;
import cn.mapway.gwt_template.shared.rpc.docker.QueryDockerAppInfoResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class QueryDockerAppInfoExecutor extends AbstractBizExecutor<QueryDockerAppInfoResponse, QueryDockerAppInfoRequest> {

    @Resource
    DockerAppService dockerAppService;

    @Override
    protected BizResult<QueryDockerAppInfoResponse> process(BizContext context, BizRequest<QueryDockerAppInfoRequest> bizParam) {
        QueryDockerAppInfoRequest request = bizParam.getData();
        log.info("QueryDockerAppInfoExecutor {}", Json.toJson(request, JsonFormat.compact()));

        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(dockerAppService.canOperate(user), "无权访问此应用");

        QueryDockerAppInfoResponse response = new QueryDockerAppInfoResponse();
        if (response.getErrors() == null) {
            response.setErrors(new ArrayList<>());
        }

        DockerAppEntity app = dockerAppService.findApp(request.getDockerAppId());
        assertNotNull(app, "找不到应用 ID: " + request.getDockerAppId());

        String absPath = app.getAbsolutePath();
        assertTrue(Strings.isNotBlank(absPath), "应用根目录未设置");

        File appDir = new File(absPath);
        assertTrue(appDir.exists() && appDir.isDirectory(), "应用目录不存在: " + absPath);

        try {
            // 1. 执行 du -hs 查询磁盘占用
            CommandUtils.ExecResult duResult = CommandUtils.exec(List.of("du", "-hs", absPath), appDir, 10);
            if (duResult.isSuccess() && Strings.isNotBlank(duResult.getStdout())) {
                String[] parts = Strings.splitIgnoreBlank(duResult.getStdout(), "[\t\\s]");
                if (parts.length > 0) {
                    response.setTotalSize(parts[0]);
                }
            } else if (Strings.isNotBlank(duResult.getStderr())) {
                response.getErrors().add("du warning: " + duResult.getStderr());
            }

            // 2. 检查 docker-compose 文件
            String composeFileName = "docker-compose.yml";
            if (!new File(absPath, composeFileName).exists()) {
                composeFileName = "docker-compose.yaml";
            }


            CommandUtils.ExecResult result = executeDockerComposeCommand(appDir.getAbsolutePath(), composeFileName, List.of("ps"));
            response.setStatus(result.getStdout());

            if (Strings.isNotBlank(result.getStderr())) {
                response.getErrors().add(result.getStderr());
            }

            result = executeDockerComposeCommand(appDir.getAbsolutePath(), composeFileName, List.of("config", "--services"));
            String out = result.getStdout();
            String[] split = Strings.split(out, false, false, '\r', '\n');
            for (String service : split) {
                response.getServices().add(service);
            }

            if (Strings.isNotBlank(result.getStderr())) {
                response.getErrors().add(result.getStderr());
            }

        } catch (Exception e) {
            log.error("查询 Docker 应用信息异常", e);
            return BizResult.error(500, "运行异常: " + e.getMessage());
        }

        return BizResult.success(response);
    }

    private CommandUtils.ExecResult executeDockerComposeCommand(String workDir, String composeFile, List<String> command) {
        List<String> finalCommand = Lang.list("docker", "compose", "-f", composeFile);
        finalCommand.addAll(command);
        CommandUtils.ExecResult dockerResult = CommandUtils.exec(finalCommand, new File(workDir), 15);
        return dockerResult;
    }
}