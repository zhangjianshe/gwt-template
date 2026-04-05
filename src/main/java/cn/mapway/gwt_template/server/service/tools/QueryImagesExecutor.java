package cn.mapway.gwt_template.server.service.tools;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.tools.QueryImagesRequest;
import cn.mapway.gwt_template.shared.rpc.tools.QueryImagesResponse;
import cn.mapway.gwt_template.shared.rpc.tools.SysImage;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * QueryImagesExecutor
 * 从资源目录中查询图片列表，可以进行一个缓存
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryImagesExecutor extends AbstractBizExecutor<QueryImagesResponse, QueryImagesRequest> {
    private List<SysImage> allImages=null;
    @Override
    protected BizResult<QueryImagesResponse> process(BizContext context, BizRequest<QueryImagesRequest> bizParam) {
        QueryImagesRequest request = bizParam.getData();
        log.info("QueryImagesExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        QueryImagesResponse response = new QueryImagesResponse();
        response.setImages(getAllImages());
        return BizResult.success(response);
    }

    private synchronized List<SysImage> getAllImages() {
        if (allImages == null) {
            allImages = new ArrayList<>();
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

            try {
                // 1. Use ** to find all files recursively in the icons directory
                Resource[] resources = resolver.getResources("classpath:static/icons/**/*");

                // Map to temporarily group images by catalog name
                java.util.Map<String, SysImage> catalogMap = new java.util.LinkedHashMap<>();

                for (Resource resource : resources) {
                    // Skip actual directory entries, we only want files
                    if (resource.getFilename() == null || resource.getDescription().contains("directory")) {
                        continue;
                    }

                    String uri = resource.getURI().toString();
                    String filename = resource.getFilename();

                    // 2. Logic to extract the folder name (catalog)
                    // We look for the part between /icons/ and the filename
                    String catalogName = "default";
                    int iconsIndex = uri.indexOf("/icons/");
                    if (iconsIndex != -1) {
                        String subPath = uri.substring(iconsIndex + 7); // skip "/icons/"
                        if (subPath.contains("/")) {
                            catalogName = subPath.substring(0, subPath.lastIndexOf("/"));
                        }
                    }

                    // 3. Grouping into your SysImage structure
                    SysImage sysImage = catalogMap.computeIfAbsent(catalogName, k -> {
                        SysImage si = new SysImage();
                        si.setCatalog(k);
                        return si;
                    });

                    // Add the image path (relative to the static folder for GWT consumption)
                    sysImage.getImages().add("icons/" + catalogName + "/" + filename);
                }

                allImages.addAll(catalogMap.values());

            } catch (IOException e) {
                log.error("Error scanning icon resources", e);
            }
        }
        return allImages;
    }
}
