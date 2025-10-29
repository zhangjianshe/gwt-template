package cn.mapway.gwt_template.shared.rpc.dns.model;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * result from cloudflare
 *  "id": "d9cd1b583404f7f5a1377244aa72191c",
 *             "name": "a.bhg.cangling.cn",
 *             "type": "A",
 *             "content": "106.38.198.176",
 *             "proxiable": true,
 *             "proxied": false,
 *             "ttl": 1,
 *             "settings": {},
 *             "meta": {},
 *             "comment": null,
 *             "tags": [],
 *             "created_on": "2024-08-29T08:11:24.936833Z",
 *             "modified_on": "2024-08-29T08:11:24.936833Z"
 */
@Getter
@Setter
public class DnsEntry implements Serializable, IsSerializable {
    String id;
    String name;
    String type;
    String content;
    Boolean proxiable;
    Boolean proxied;
    Long ttl;
    String comment;
    List<String> tags;
    String create_on;
    String modified_on;
}
