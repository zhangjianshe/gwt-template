package cn.mapway.gwt_template.shared.rpc.tools;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * MarkdownToHtmlRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("MarkdownToHtmlRequest")
public class MarkdownToHtmlRequest implements Serializable, IsSerializable {
    String markdown;
}
