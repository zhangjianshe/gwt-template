package cn.mapway.gwt_template.server.service.project;

import cn.mapway.gwt_template.server.config.castor.PgObjectToPageManifest;
import cn.mapway.gwt_template.shared.db.DevProjectPageCommitEntity;
import cn.mapway.gwt_template.shared.db.DevProjectPageEntity;
import cn.mapway.gwt_template.shared.db.DevProjectPageSectionEntity;
import org.nutz.castor.Castors;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class WikiService {
    static {
        // 强制告诉 Nutz：遇到 PGobject 且目标是 PageManifest 时，用 Json 序列化工具处理
        Castors.me().addCastor(PgObjectToPageManifest.class);
    }

    @Resource
    Dao dao;

    /**
     * 根据 CommitId 一次性加载整个页面的所有内容
     */
    public List<DevProjectPageSectionEntity> loadFullPage(String commitId) {
        String sqlStr = "SELECT s.* FROM " + DevProjectPageSectionEntity.TBL_NAME + " s " +
                "JOIN (SELECT * FROM jsonb_to_recordset(" +
                "  (SELECT (manifest->'sections')::jsonb " + // 【关键】这里增加 ::jsonb 强制转换
                "   FROM " + DevProjectPageCommitEntity.TBL_NAME +
                "   WHERE " + DevProjectPageCommitEntity.FLD_ID + " = @id)" +
                ") AS x(sectionId text, version text)) m " +
                "ON s." + DevProjectPageSectionEntity.FLD_VERSION + " = m.version " +
                "ORDER BY array_position(ARRAY(" +
                "  SELECT (jsonb_array_elements((manifest->'sections')::jsonb)->>'sectionId') " + // 【关键】这里也增加转换
                "  FROM " + DevProjectPageCommitEntity.TBL_NAME +
                "  WHERE id = @id), s." + DevProjectPageSectionEntity.FLD_SECTION_ID + ")";

        Sql sql = Sqls.create(sqlStr);
        sql.params().set("id", commitId);
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao.getEntity(DevProjectPageSectionEntity.class));
        dao.execute(sql);

        return sql.getList(DevProjectPageSectionEntity.class);
    }

    public String calculateId(DevProjectPageCommitEntity commit) {
        // 1. 将关键信息拼接成一个唯一的字符串
        // 建议使用特定的分隔符防止碰撞
        StringBuilder sb = new StringBuilder();
        sb.append(commit.getPageId()).append("|")
                .append(commit.getParentId() == null ? "root" : commit.getParentId()).append("|")
                .append(commit.getAuthorId()).append("|")
                .append(commit.getMessage()).append("|");

        // 2. 将清单中的所有 sectionId:versionId 排序并拼接
        // 必须排序，确保相同的清单生成相同的哈希
        commit.getManifest().getSections().stream()
                .sorted((a, b) -> a.getSectionId().compareTo(b.getSectionId()))
                .forEach(s -> sb.append(s.getSectionId()).append(":").append(s.getVersion()).append(","));

        // 3. 计算 SHA-256 并转为 Hex 字符串
        return Lang.sha256(sb.toString());
    }

    public DevProjectPageEntity findPageById(String pageId) {
        return dao.fetch(DevProjectPageEntity.class, pageId);
    }

    public String calculateVersionId(DevProjectPageSectionEntity pageSection) {
        // 1. 将关键信息拼接成一个唯一的字符串
        // 建议使用特定的分隔符防止碰撞
        StringBuilder sb = new StringBuilder();
        sb.append(pageSection.getPageId()).append("|")
                .append(pageSection.getKind()).append("|")
                .append(pageSection.getSectionId()).append("|")
                .append(pageSection.getContent()).append("|");
        if (pageSection.getData() != null) {
            String md5 = Lang.md5(Streams.wrap(pageSection.getData()));
            sb.append(md5);
        }
        return Lang.sha256(sb.toString());
    }
}
