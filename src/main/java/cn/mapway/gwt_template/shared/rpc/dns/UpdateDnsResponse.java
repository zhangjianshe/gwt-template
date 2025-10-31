package cn.mapway.gwt_template.shared.rpc.dns;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.rpc.dns.model.DnsEntry;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateDnsResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateDnsResponse")
public class UpdateDnsResponse implements Serializable, IsSerializable {
    DnsEntry entry;
}
