package cn.mapway.gwt_template.server.service.git;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.springframework.stereotype.Service;
import java.util.Arrays;

@Service
public class MarkdownService {

    public String renderHtml(String markdown) {
        if (markdown == null || markdown.isEmpty()) return "";

        MutableDataSet options = new MutableDataSet();

        // Setup GitHub Flavored Markdown (GFM) extensions
        options.set(Parser.EXTENSIONS, Arrays.asList(
                TablesExtension.create(),
                StrikethroughExtension.create()
        ));

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        // Parse and Render
        return renderer.render(parser.parse(markdown));
    }
}