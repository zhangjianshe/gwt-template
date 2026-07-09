package cn.mapway.gwt_template.shared.rpc.powerdns;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.rpc.powerdns.model.PowerDnsRRSet;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * CreateOrUpdateRecordRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("CreateOrUpdateRecordRequest")
public class CreateOrUpdateRecordRequest implements Serializable, IsSerializable {
    /**
     * such as tjj.cn.
     */
    String zoneId;
    private PowerDnsRRSet RRSet;
}
