package cn.mapway.gwt_template.server.service.repository;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.git.GitRepoService;
import cn.mapway.gwt_template.server.service.git.MarkdownService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevRepositoryEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.repository.ReadRepoFileRequest;
import cn.mapway.gwt_template.shared.rpc.repository.ReadRepoFileResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * ReadRepoFileExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class ReadRepoFileExecutor extends AbstractBizExecutor<ReadRepoFileResponse, ReadRepoFileRequest> {
    @Resource
    RepositoryService repositoryService;
    @Resource
    GitRepoService gitRepoService;
    @Resource
    MarkdownService markdownService;

    @Override
    protected BizResult<ReadRepoFileResponse> process(BizContext context, BizRequest<ReadRepoFileRequest> bizParam) {
        ReadRepoFileRequest request = bizParam.getData();
        log.info("ReadRepoFileExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getRepositoryId()), "没有提供repositoryId");
        assertTrue(Strings.isNotBlank(request.getFilePathName()), "没有文件");
        CommonPermission permission = repositoryService.findUserPermissionInRepository(user.getUser().getUserId(), request.getRepositoryId());
        assertTrue(permission.canRead(), "没有授权读取文件");
        DevRepositoryEntity project = repositoryService.findRepositoryById(request.getRepositoryId());

        BizResult<String> fileContent = gitRepoService.getFileContent(project.getOwnerName(), project.getName(), request.getFilePathName(),request.getRefName());
        if (fileContent.isSuccess()) {
            ReadRepoFileResponse response = new ReadRepoFileResponse();
            response.setText(fileContent.getData());
            if (request.getToHtml()) {
                response.setText(markdownService.renderHtml(fileContent.getData()));
            }
            return BizResult.success(response);
        } else {
            return fileContent.asBizResult();
        }
    }
}
