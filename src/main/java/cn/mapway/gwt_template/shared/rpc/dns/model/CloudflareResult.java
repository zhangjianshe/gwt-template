package cn.mapway.gwt_template.shared.rpc.dns.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CloudflareResult<T> {
    T result;
    boolean success;
    List<CloudflareError> errors;
    List<String> messages;
    CloudflareResultInfo result_info;
}
