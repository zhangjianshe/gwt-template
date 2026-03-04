package cn.mapway.gwt_template.server.service.repository;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.git.GitRepoService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.rpc.repository.ImportRepoRequest;
import cn.mapway.gwt_template.shared.rpc.repository.ImportRepoResponse;
import cn.mapway.gwt_template.shared.rpc.repository.RepositoryStatus;
import cn.mapway.gwt_template.shared.rpc.user.CommonPermission;
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

    @Override
    protected BizResult<ImportRepoResponse> process(BizContext context, BizRequest<ImportRepoRequest> bizParam) {
        ImportRepoRequest request = bizParam.getData();
        log.info("ImportRepoExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getProjectId()), "没有项目ID");
        assertTrue(Strings.isNotBlank(request.getRepoUrl()), "没有仓库地址");
        CommonPermission permission = repositoryService.findUserPermissionInProject(user.getUser().getUserId(), request.getProjectId());
        assertTrue(permission.isAdmin(), "没有导入仓库的权限");


        DevProjectEntity project = repositoryService.findProjectById(request.getProjectId());

        if(!RepositoryStatus.PS_INIT.getCode().equals(project.getStatus())) {
            return BizResult.error(500,"项目状态目前不允许导入仓库");
        }
        DevProjectEntity temp=new DevProjectEntity();
        temp.setId(project.getId());


        // Start the import in the background
        gitRepoService.importRepo(project, request);

        ImportRepoResponse importRepoResponse = new ImportRepoResponse();
        importRepoResponse.setMessage("开始导入");
        return BizResult.success(importRepoResponse);

    }
}
