package cn.mapway.gwt_template.shared.rpc.powerdns;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.rpc.powerdns.model.PowerDnsRRSet;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryRecordsResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryRecordsResponse")
public class QueryRecordsResponse implements Serializable, IsSerializable {
    private List<PowerDnsRRSet> rrsets;
}
