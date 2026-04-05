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
            java.util.Map<String, List<String>> catalogMap = new java.util.LinkedHashMap<>();

            try {
                // 使用通配符获取所有图片
                Resource[] resources = resolver.getResources("classpath*:static/icons/**/*.*");

                for (Resource resource : resources) {
                    // 排除文件夹资源
                    if (!resource.isReadable() || resource.contentLength() <= 0) {
                        continue;
                    }

                    // 获取并解码 URI
                    String uri = java.net.URLDecoder.decode(resource.getURI().toString(), "UTF-8");

                    // 统一路径分隔符为 /
                    uri = uri.replace("\\", "/");

                    String iconsMarker = "/static/icons/";
                    int iconsIndex = uri.indexOf(iconsMarker);

                    if (iconsIndex != -1) {
                        // 截取 icons 之后的路径： 例如 "分类/图片.png"
                        String subPath = uri.substring(iconsIndex + iconsMarker.length());

                        String catalogName = "default";
                        String filename;

                        if (subPath.contains("/")) {
                            catalogName = subPath.substring(0, subPath.lastIndexOf("/"));
                            filename = subPath.substring(subPath.lastIndexOf("/") + 1);
                        } else {
                            filename = subPath;
                        }

                        // 填充结果
                        catalogMap.computeIfAbsent(catalogName, k -> new ArrayList<>())
                                .add("icons/" + catalogName + "/" + filename);
                    }
                }

                // 转换为 SysImage 列表
                catalogMap.forEach((catalog, imgs) -> {
                    SysImage si = new SysImage();
                    si.setCatalog(catalog);
                    si.setImages(imgs);
                    allImages.add(si);
                });

            } catch (IOException e) {
                log.error("扫描图标资源出错", e);
            }
        }
        return allImages;
    }
}
