package cn.mapway.gwt_template.shared.rpc.tools;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * MarkdownToHtmlResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("MarkdownToHtmlResponse")
public class MarkdownToHtmlResponse implements Serializable, IsSerializable {
    String html;
}
