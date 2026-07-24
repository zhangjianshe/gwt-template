package cn.mapway.gwt_template.shared.rpc.project.module;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PreviewData implements Serializable, IsSerializable {
    String mimeType;
    String body;
    String fileName;
    Double fileSize;
    String resourceId;
    String url;
    // 根据内容 判断出来的后缀名称
    String suffixName;
}
