package cn.mapway.gwt_template.server.service.file;


import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.config.SystemConfigService;
import cn.mapway.gwt_template.shared.rpc.file.UploadFileRequest;
import cn.mapway.gwt_template.shared.rpc.file.UploadReturnResponse;
import lombok.extern.slf4j.Slf4j;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * FileUploadExecutor
 *
 * @author zhangjianshe@gmail.com
 */
@Slf4j
@Component
public class FileUploadExecutor extends AbstractBizExecutor<UploadReturnResponse, UploadFileRequest> {


    @Resource
    SystemConfigService configService;

    @Override
    protected BizResult<UploadReturnResponse> process(BizContext context, BizRequest<UploadFileRequest> bizParam) {
        UploadFileRequest request = bizParam.getData();
        log.info("上传文件{},{},{}", request.getExtra(), request.getRelPath(), request.getFile().getOriginalFilename());

        if (!acceptable(request.getFile().getOriginalFilename())) {
            return BizResult.error(500, "不支持此文件格式上传" + request.getFile().getOriginalFilename());
        }

        String targetPath = FileCustomUtils.concatPath(configService.getUploadPath(), "upload");
        String relpath = (Strings.isBlank(request.getRelPath()) ? "" : request.getRelPath());
        targetPath=FileCustomUtils.concatPath(targetPath, relpath);

        Files.makeDir(new File(targetPath));
        try (InputStream input = request.getFile().getInputStream()) {
            File tempFile = new File(targetPath + "/" + R.UU16() + ".temp");
            log.info("upload h is {}", targetPath);
            Streams.writeAndClose(new FileOutputStream(tempFile), input);
            String md5 = Lang.md5(tempFile);
            String prefix = md5.substring(0, 2);
            String path = targetPath + "/" + prefix + "/";
            String fileName = md5.substring(2) + Files.getSuffix(request.getFile().getOriginalFilename());
            Files.makeDir(new File(path));
            Files.move(tempFile, new File(path + fileName));
            UploadReturnResponse response = new UploadReturnResponse();
            response.setMsg("");
            response.setRetCode(0);
            response.size = request.getFile().getSize();
            response.md5 = md5;
            response.extra = request.getExtra();
            response.setFileName(request.getFile().getOriginalFilename());
            response.setRelPath("/upload/" + relpath + "/" + prefix + "/" + fileName);
            tempFile.delete();
            return BizResult.success(response);

        } catch (Exception e) {
            log.error(e.getMessage());
            return BizResult.error(500, e.getMessage());
        }
    }

    private boolean acceptable(String originalFilename) {
        return true;
        /* String suffix = Files.getSuffixName(originalFilename);
        if (suffix == null) {
            return false;
        }
        suffix = suffix.toLowerCase();
        BizResult<GlobalConfig> globalConfig = configService.getGlobalConfig();
        if (globalConfig.isSuccess()) {
            String unSupportFileSuffix = globalConfig.getData().unSupportFileSuffix;
            if (!Strings.isBlank(unSupportFileSuffix)) {
                return !unSupportFileSuffix.toLowerCase().contains(suffix);
            }
            return true;
        }
        return true;*/
    }
}
