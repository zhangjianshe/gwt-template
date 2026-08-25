package cn.mapway.gwt_template.server.service.app;

import lombok.Data;
import org.nutz.lang.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class Domain {
    String main;
    List<String> sans;

    public static Domain parseDomain(String domains) {
        if (Strings.isBlank(domains)) {
            return null;
        }
        String[] split = Strings.split(domains, false, ',');
        Domain domain = new Domain();
        domain.main = split[0];
        if (split.length > 1) {
            domain.sans = new ArrayList<>();
            domain.sans.addAll(Arrays.asList(split).subList(1, split.length));
            return domain;
        } else {
            domain.sans = null;
            return domain;
        }

    }
}
