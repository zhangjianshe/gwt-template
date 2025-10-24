package cn.mapway.gwt_template.shared.rpc.config;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.SysConfigEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryConfigListResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryConfigListResponse")
public class QueryConfigListResponse implements Serializable, IsSerializable {
    List<SysConfigEntity> configs;
}
