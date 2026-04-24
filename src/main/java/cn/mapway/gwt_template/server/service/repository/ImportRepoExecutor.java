package cn.mapway.gwt_template.server.service.repository;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.dev.UpdateRepositoryExecutor;
import cn.mapway.gwt_template.server.service.git.GitRepoService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevRepositoryEntity;
import cn.mapway.gwt_template.shared.rpc.dev.UpdateRepositoryRequest;
import cn.mapway.gwt_template.shared.rpc.dev.UpdateRepositoryResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.repository.ImportRepoRequest;
import cn.mapway.gwt_template.shared.rpc.repository.ImportRepoResponse;
import cn.mapway.gwt_template.shared.rpc.repository.RepositoryStatus;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * ImportRepoExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class ImportRepoExecutor extends AbstractBizExecutor<ImportRepoResponse, ImportRepoRequest> {
    @Resource
    RepositoryService repositoryService;
    @Resource
    GitRepoService gitRepoService;
    @Resource
    UpdateRepositoryExecutor updateRepositoryExecutor;

    @Override
    protected BizResult<ImportRepoResponse> process(BizContext context, BizRequest<ImportRepoRequest> bizParam) {
        ImportRepoRequest request = bizParam.getData();
        log.info("ImportRepoExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getRepoUrl()), "没有仓库地址");

        if (Strings.isBlank(request.getRepositoryId())) {
            assertTrue(Strings.isNotBlank(request.getNewRepositoryName()), "请为新的仓库输入名称");
            UpdateRepositoryRequest createRepoRequest = new UpdateRepositoryRequest();
            DevRepositoryEntity repo = new DevRepositoryEntity();
            repo.setName(request.getNewRepositoryName());
            repo.setFullName(request.getNewRepositoryName());
            createRepoRequest.setRepository(repo);
            BizResult<UpdateRepositoryResponse> execute = updateRepositoryExecutor.execute(context, BizRequest.wrap("", createRepoRequest));
            if (execute.isFailed()) {
                return execute.asBizResult();
            }
            request.setRepositoryId(execute.getData().getRepository().getId());
        }

        CommonPermission permission = repositoryService.findUserPermissionInRepository(user.getUser().getUserId(), request.getRepositoryId());
        assertTrue(permission.isSuper(), "没有导入仓库的权限");


        DevRepositoryEntity project = repositoryService.findRepositoryById(request.getRepositoryId());

        if (!RepositoryStatus.PS_INIT.getCode().equals(project.getStatus())) {
            return BizResult.error(500, "项目状态目前不允许导入仓库");
        }

        // Start the import in the background
        BizResult<ImportRepoResponse> bizResult = gitRepoService.importRepo(project, request);
        if (bizResult.isFailed())
        {
            return bizResult.asBizResult();
        }
        ImportRepoResponse importRepoResponse = new ImportRepoResponse();
        importRepoResponse.setMessage("开始导入");
        return BizResult.success(importRepoResponse);

    }
}
