package cn.mapway.gwt_template.server.service.soft;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.config.SystemConfigService;
import cn.mapway.gwt_template.shared.db.SysSoftwareEntity;
import cn.mapway.gwt_template.shared.db.SysSoftwareFileEntity;
import cn.mapway.gwt_template.shared.rpc.soft.QuerySoftwareSetManifestRequest;
import cn.mapway.gwt_template.shared.rpc.soft.QuerySoftwareSetManifestResponse;
import cn.mapway.gwt_template.shared.rpc.soft.SoftwareFileManifest;
import cn.mapway.gwt_template.shared.rpc.soft.SoftwareManifest;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Open query: software-set manifest for keeper sync.
 */
@Component
@Slf4j
public class QuerySoftwareSetManifestExecutor extends AbstractBizExecutor<QuerySoftwareSetManifestResponse, QuerySoftwareSetManifestRequest> {
    @Resource
    Dao dao;
    @Resource
    SystemConfigService systemConfigService;

    @Override
    protected BizResult<QuerySoftwareSetManifestResponse> process(BizContext context, BizRequest<QuerySoftwareSetManifestRequest> bizParam) {
        QuerySoftwareSetManifestRequest request = bizParam.getData();
        assertNotNull(request, "没有请求参数");
        String setName = SoftwareStorage.normalizeSet(request.getSet());
        assertTrue(Strings.isNotBlank(setName), "没有软件集名称");
        assertTrue(!setName.contains("/") && !setName.contains("\\") && !setName.contains(".."), "软件集名称不合法");

        List<SysSoftwareEntity> softwares = dao.query(SysSoftwareEntity.class,
                Cnd.where(SysSoftwareEntity.FLD_SOFTWARE_SET, "=", setName)
                        .asc(SysSoftwareEntity.FLD_NAME));

        List<SoftwareManifest> items = new ArrayList<>();
        String uploadRoot = systemConfigService.getUploadRoot();
        for (SysSoftwareEntity software : softwares) {
            items.add(toManifest(software, uploadRoot));
        }

        QuerySoftwareSetManifestResponse response = new QuerySoftwareSetManifestResponse();
        response.setName(setName);
        response.setSoftwares(items);
        return BizResult.success(response);
    }

    private SoftwareManifest toManifest(SysSoftwareEntity software, String uploadRoot) {
        SoftwareManifest item = new SoftwareManifest();
        item.setId(software.getId());
        item.setName(software.getName());
        item.setCode(SoftwareStorage.normalizeCode(software.getCode()));
        if (Strings.isBlank(item.getCode())) {
            item.setCode(software.getId());
        }
        item.setSoftwareSet(software.getSoftwareSet());
        item.setSummary(software.getSummary());
        item.setLogo(software.getLogo());

        List<SysSoftwareFileEntity> files = dao.query(SysSoftwareFileEntity.class,
                Cnd.where(SysSoftwareFileEntity.FLD_SOFTWARE_ID, "=", software.getId())
                        .desc(SysSoftwareFileEntity.FLD_VERSION)
                        .asc(SysSoftwareFileEntity.FLD_NAME)
                        .asc(SysSoftwareFileEntity.FLD_OS)
                        .asc(SysSoftwareFileEntity.FLD_ARCH));
        List<SoftwareFileManifest> fileItems = new ArrayList<>();
        for (SysSoftwareFileEntity file : files) {
            fileItems.add(toFileManifest(file, uploadRoot));
        }
        item.setFiles(fileItems);
        return item;
    }

    private SoftwareFileManifest toFileManifest(SysSoftwareFileEntity file, String uploadRoot) {
        if (Strings.isBlank(file.getHash())) {
            fillHash(file, uploadRoot);
        }
        SoftwareFileManifest item = new SoftwareFileManifest();
        item.setId(file.getId());
        item.setName(file.getName());
        item.setVersion(file.getVersion());
        item.setOs(file.getOs());
        item.setArch(file.getArch());
        item.setSize(file.getSize());
        item.setHash(file.getHash());
        item.setUrl(file.getLocation());
        item.setSummary(file.getSummary());
        return item;
    }

    private void fillHash(SysSoftwareFileEntity file, String uploadRoot) {
        File disk = SoftwareStorage.toDiskFile(uploadRoot, file.getLocation());
        if (disk == null || !disk.isFile()) {
            return;
        }
        try {
            String hash = SoftwareStorage.sha256Hex(disk);
            file.setHash(hash);
            dao.update(file);
        } catch (Exception e) {
            log.warn("compute software file hash failed {} {}", file.getId(), e.getMessage());
        }
    }
}
