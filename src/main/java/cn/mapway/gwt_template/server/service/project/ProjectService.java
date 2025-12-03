package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.db.DevNodeEntity;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import org.nutz.dao.Dao;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.List;

@Service
public class ProjectService {
    @Resource
    Dao dao;

    public BizResult<DevProjectEntity> saveOrUpdateProject(DevProjectEntity project) {
        if (Strings.isBlank(project.getId())) {
            project.setId(R.UU16());
            project.setCreateTime(new Timestamp(
                    System.currentTimeMillis()
            ));
            dao.insert(project);
            return BizResult.success(project);
        } else {
            dao.updateIgnoreNull(project);
            project = dao.fetch(DevProjectEntity.class, project.getId());
            return BizResult.success(project);
        }
    }

    public List<DevProjectEntity> allProjects(Long userId) {
        return dao.query(DevProjectEntity.class, null);
    }

    public List<DevNodeEntity> allNodes() {
        return dao.query(DevNodeEntity.class, null);
    }

    public BizResult<DevNodeEntity> saveOrUpdateNode(DevNodeEntity node) {
        if (Strings.isBlank(node.getId())) {
            node.setId(R.UU16());
            node.setCreateTime(new Timestamp(
                    System.currentTimeMillis()
            ));
            dao.insert(node);
            return BizResult.success(node);
        } else {
            dao.updateIgnoreNull(node);
            node = dao.fetch(DevNodeEntity.class, node.getId());
            return BizResult.success(node);
        }
    }
}
