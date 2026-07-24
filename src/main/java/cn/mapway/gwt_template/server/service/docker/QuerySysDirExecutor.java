package cn.mapway.gwt_template.server.service.docker;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.docker.QuerySysDirRequest;
import cn.mapway.gwt_template.shared.rpc.docker.QuerySysDirResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.ResItem;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * QuerySysDirExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QuerySysDirExecutor extends AbstractBizExecutor<QuerySysDirResponse, QuerySysDirRequest> {
    @Resource
    DockerAppService dockerAppService;

    @Override
    protected BizResult<QuerySysDirResponse> process(BizContext context, BizRequest<QuerySysDirRequest> bizParam) {
        QuerySysDirRequest request = bizParam.getData();
        log.info("QuerySysDirExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        assertTrue(dockerAppService.canOperate(user), "没有授权操作");


        if (Strings.isBlank(request.getPath())) {
            request.setPath("/");
        }
        QuerySysDirResponse response = new QuerySysDirResponse();

        File file = new File(request.getPath());
        if (file.exists() && !file.isFile()) {
            response.setPath(request.getPath());
            File[] files = file.listFiles(File::isDirectory);
            if (files == null || files.length == 0) {
                response.setDirs(new ArrayList<>());
            } else {
                List<ResItem> resItems = new ArrayList<>();
                for (File fileItem : files) {
                    ResItem resItem = new ResItem();
                    resItem.setPathName(fileItem.getName());
                    resItem.setIsDir(fileItem.isDirectory());
                    resItem.setFileSize((double) fileItem.length());
                    resItem.setLastModified((double) fileItem.lastModified());
                    resItems.add(resItem);
                }
                response.setDirs(resItems);
            }
            return BizResult.success(response);
        } else {
            return BizResult.error(500, "目录不存在");
        }
    }
}
