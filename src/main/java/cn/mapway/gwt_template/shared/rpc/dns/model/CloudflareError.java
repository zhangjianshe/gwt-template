package cn.mapway.gwt_template.shared.rpc.dns.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CloudflareError {
    Long code;
    String message;
}
