package cn.mapway.gwt_template.shared.rpc.dns;

import cn.mapway.gwt_template.shared.rpc.dns.model.DnsEntry;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryDnsResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
public class QueryDnsResponse implements Serializable, IsSerializable {
    List<DnsEntry> dnsList;
}
