package cn.mapway.gwt_template.shared.rpc.dns.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CloudflareResultInfo {

    Integer page;
    Integer per_page;
    Integer count;
    Integer total_count;
    Integer total_pages;
}
