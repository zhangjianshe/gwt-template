package cn.mapway.gwt_template.server.service.soft;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.config.SystemConfigService;
import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.SysSoftwareEntity;
import cn.mapway.gwt_template.shared.db.SysSoftwareFileEntity;
import cn.mapway.gwt_template.shared.rpc.soft.UploadSoftwareFileRequest;
import cn.mapway.gwt_template.shared.rpc.soft.UploadSoftwareFileResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;

/**
 * UploadSoftwareFileExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UploadSoftwareFileExecutor extends AbstractBizExecutor<UploadSoftwareFileResponse, HttpServletRequest> {
    private static final long MAX_UPLOAD_BYTES = 1000L * 1024 * 1024;

    @Resource
    Dao dao;
    @Resource
    SystemConfigService systemConfigService;

    @Override
    protected BizResult<UploadSoftwareFileResponse> process(BizContext context, BizRequest<HttpServletRequest> bizParam) {
        HttpServletRequest httpRequest = bizParam.getData();
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        if (httpRequest == null || !ServletFileUpload.isMultipartContent(httpRequest)) {
            return BizResult.error(500, "没有文件 file 字段");
        }

        UploadSoftwareFileRequest form = new UploadSoftwareFileRequest();
        String originalFilename = null;
        File tempFile = null;
        File savedFile = null;

        try {
            ServletFileUpload upload = new ServletFileUpload();
            upload.setHeaderEncoding("UTF-8");
            upload.setFileSizeMax(MAX_UPLOAD_BYTES);
            upload.setSizeMax(MAX_UPLOAD_BYTES);
            upload.setFileCountMax(32);
            FileItemIterator iter = upload.getItemIterator(httpRequest);
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                if (item.isFormField()) {
                    String value = Streams.asString(item.openStream(), "UTF-8");
                    applyField(form, item.getFieldName(), value);
                    continue;
                }
                if (!"file".equals(item.getFieldName())) {
                    item.openStream().close();
                    continue;
                }
                originalFilename = item.getName();
                File dest = resolveImmediateDest(form, originalFilename);
                try (InputStream in = item.openStream()) {
                    if (dest != null) {
                        Files.createDirIfNoExists(dest.getParent());
                        java.nio.file.Files.copy(in, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        savedFile = dest;
                    } else {
                        File tmpDir = new File(systemConfigService.getUploadRoot());
                        Files.createDirIfNoExists(tmpDir.getAbsolutePath());
                        tempFile = File.createTempFile("software-upload-", ".part", tmpDir);
                        java.nio.file.Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        } catch (Exception e) {
            deleteQuietly(tempFile);
            log.error("upload software file failed", e);
            return BizResult.error(500, e.getMessage());
        }

        try {
            assertTrue(Strings.isNotBlank(form.getToken()), "没有授权操作");
            SysSoftwareEntity software = dao.fetch(SysSoftwareEntity.class, Cnd.where(SysSoftwareEntity.FLD_TOKEN, "=", form.getToken()));
            assertTrue(software != null, "没有软件信息" + form.getToken());
            assertTrue(Strings.isNotBlank(form.getOs()), "没有OS");
            assertTrue(Strings.isNotBlank(form.getArch()), "没有Arch");
            assertTrue(Strings.isNotBlank(form.getVersion()), "没有Version");
            assertTrue(Strings.isNotBlank(form.getName()), "没有Name");
            assertTrue(savedFile != null || tempFile != null, "没有文件 file 字段");

            String diskName = safeFileName(originalFilename, form.getName());
            assertTrue(Strings.isNotBlank(diskName), "没有文件名");

            String targetPath = FileCustomUtils.concatPath(systemConfigService.getUploadRoot(), "software");
            Files.createDirIfNoExists(targetPath);
            String versionPath = FileCustomUtils.concatPath(targetPath, software.getId(), form.getVersion());
            Files.createDirIfNoExists(versionPath);

            SysSoftwareFileEntity existing = findExisting(software.getId(), form);

            File targetFile;
            if (existing != null && Strings.isNotBlank(existing.getLocation())) {
                targetFile = new File(FileCustomUtils.concatPath(systemConfigService.getUploadRoot(), existing.getLocation()));
                Files.createDirIfNoExists(targetFile.getParent());
            } else {
                targetFile = new File(versionPath, diskName);
            }

            File source = savedFile != null ? savedFile : tempFile;
            if (source != null && !source.getAbsolutePath().equals(targetFile.getAbsolutePath())) {
                try {
                    Files.createDirIfNoExists(targetFile.getParent());
                    java.nio.file.Files.move(source.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    log.error("save software file failed", e);
                    return BizResult.error(500, e.getMessage());
                }
                if (source == tempFile) {
                    tempFile = null;
                }
                savedFile = targetFile;
            }

            long size = targetFile.length();
            String location = toSoftwareLocation(targetPath, targetFile);

            UploadSoftwareFileResponse response = new UploadSoftwareFileResponse();
            if (existing != null) {
                existing.setSize(size);
                existing.setLocation(location);
                existing.setOs(form.getOs());
                existing.setArch(form.getArch());
                existing.setSummary(form.getSummary());
                existing.setCreateTime(new Timestamp(System.currentTimeMillis()));
                dao.update(existing);
                log.info("software file exists, overwrite {} {}", form.getName(), location);
                response.setUrl(existing.getLocation());
                return BizResult.success(response);
            }

            SysSoftwareFileEntity fileEntity = new SysSoftwareFileEntity();
            fileEntity.setId(R.UU16());
            fileEntity.setName(form.getName());
            fileEntity.setSoftwareId(software.getId());
            fileEntity.setSize(size);
            fileEntity.setOs(form.getOs());
            fileEntity.setSummary(form.getSummary());
            fileEntity.setArch(form.getArch());
            fileEntity.setVersion(form.getVersion());
            fileEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
            fileEntity.setLocation(location);
            dao.insert(fileEntity);
            response.setUrl(fileEntity.getLocation());
            return BizResult.success(response);
        } finally {
            deleteQuietly(tempFile);
        }
    }

    private SysSoftwareFileEntity findExisting(String softwareId, UploadSoftwareFileRequest form) {
        return dao.fetch(SysSoftwareFileEntity.class,
                Cnd.where(SysSoftwareFileEntity.FLD_SOFTWARE_ID, "=", softwareId)
                        .and(SysSoftwareFileEntity.FLD_VERSION, "=", form.getVersion())
                        .and(SysSoftwareFileEntity.FLD_NAME, "=", form.getName())
                        .and(SysSoftwareFileEntity.FLD_ARCH, "=", form.getArch())
                        .and(SysSoftwareFileEntity.FLD_OS, "=", form.getOs()));
    }

    private File resolveImmediateDest(UploadSoftwareFileRequest form, String originalFilename) {
        if (Strings.isBlank(form.getToken()) || Strings.isBlank(form.getVersion())) {
            return null;
        }
        SysSoftwareEntity software = dao.fetch(SysSoftwareEntity.class, Cnd.where(SysSoftwareEntity.FLD_TOKEN, "=", form.getToken()));
        if (software == null) {
            return null;
        }
        String diskName = safeFileName(originalFilename, form.getName());
        if (Strings.isBlank(diskName)) {
            return null;
        }
        if (Strings.isNotBlank(form.getName()) && Strings.isNotBlank(form.getOs()) && Strings.isNotBlank(form.getArch())) {
            SysSoftwareFileEntity existing = findExisting(software.getId(), form);
            if (existing != null && Strings.isNotBlank(existing.getLocation())) {
                return new File(FileCustomUtils.concatPath(systemConfigService.getUploadRoot(), existing.getLocation()));
            }
        }
        String versionPath = FileCustomUtils.concatPath(systemConfigService.getUploadRoot(), "software", software.getId(), form.getVersion());
        return new File(versionPath, diskName);
    }

    private void applyField(UploadSoftwareFileRequest form, String fieldName, String value) {
        if (fieldName == null) {
            return;
        }
        switch (fieldName) {
            case "token":
                form.setToken(value);
                break;
            case "version":
                form.setVersion(value);
                break;
            case "name":
                form.setName(value);
                break;
            case "summary":
                form.setSummary(value);
                break;
            case "os":
                form.setOs(value);
                break;
            case "arch":
                form.setArch(value);
                break;
            default:
                break;
        }
    }

    private String safeFileName(String filename, String fallback) {
        String name = filename;
        if (Strings.isNotBlank(name)) {
            name = name.replace('\\', '/');
            int slash = name.lastIndexOf('/');
            if (slash >= 0) {
                name = name.substring(slash + 1);
            }
        }
        if (Strings.isBlank(name)) {
            name = fallback;
        }
        if (Strings.isBlank(name) || name.contains("..")) {
            return null;
        }
        return name;
    }

    private String toSoftwareLocation(String softwareRoot, File targetFile) {
        String absolute = targetFile.getAbsolutePath().replace('\\', '/');
        String root = new File(softwareRoot).getAbsolutePath().replace('\\', '/');
        String relative = absolute.substring(root.length());
        if (!relative.startsWith("/")) {
            relative = "/" + relative;
        }
        return "/software" + relative;
    }

    private void deleteQuietly(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}
