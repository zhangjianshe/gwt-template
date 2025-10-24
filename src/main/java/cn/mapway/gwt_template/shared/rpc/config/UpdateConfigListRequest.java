package cn.mapway.gwt_template.shared.rpc.config;

import cn.mapway.gwt_template.shared.db.SysConfigEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * UpdateConfigListRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
public class UpdateConfigListRequest implements Serializable, IsSerializable {
    List<SysConfigEntity> configList;
}
