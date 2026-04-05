package cn.mapway.gwt_template.shared.rpc.tools;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统图标
 */
@Getter
@Setter
public class SysImage implements Serializable {
    String catalog;
    List<String> images;
    public SysImage() {
        images = new ArrayList<>();
    }
}
