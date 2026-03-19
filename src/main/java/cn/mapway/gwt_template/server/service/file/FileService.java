package cn.mapway.gwt_template.server.service.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.nutz.lang.Strings;

import java.io.File;

@Slf4j
public class FileService {
    public static String previewExcel(File file, String url) {
        StringBuilder sb = new StringBuilder();
        sb.append("<style>")
                .append(".excel-tab-btn { padding: 8px 16px; cursor: pointer; border: 1px solid #ddd; background: #f8f9fa; margin-right: 5px; border-bottom: none; border-radius: 4px 4px 0 0; font-size:13px; }")
                .append(".excel-tab-btn.active { background: transparent; font-weight: bold; border-top: 2px solid #007bff; border-left: 1px solid #ddd; border-right: 1px solid #ddd; }")
                .append(".sheet-container { display: none; padding: 10px 0; }")
                .append(".sheet-container.active { display: block; }")
                .append(".excel-table { border-collapse: collapse; width: auto; min-width: 100%; font-family: sans-serif; font-size: 13px; background: transparent; }")
                .append(".excel-table th, .excel-table td { border: 1px solid #e0e0e0; padding: 8px; white-space: nowrap; text-align: left; }")
                .append(".excel-table th { background: #f4f4f4; position: sticky; top: 0; z-index: 10; }")
                .append(".excel-cell-truncate { max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }")
                .append("</style>");

        sb.append("<script>function switchSheet(idx, total) {")
                .append("for(var i=0; i<total; i++) {")
                .append("document.getElementById('sheet-'+i).style.display='none';")
                .append("document.getElementById('btn-'+i).className='excel-tab-btn';")
                .append("} document.getElementById('sheet-'+idx).style.display='block';")
                .append("document.getElementById('btn-'+idx).className='excel-tab-btn active';")
                .append("}</script>");

        try (Workbook workbook = WorkbookFactory.create(file)) {
            int sheetCount = workbook.getNumberOfSheets();
            sb.append("<div style='display:flex; margin-bottom:-1px; padding-left:10px;'>");
            for (int i = 0; i < sheetCount; i++) {
                String cls = (i == 0) ? "excel-tab-btn active" : "excel-tab-btn";
                sb.append("<button id='btn-").append(i).append("' class='").append(cls).append("' onclick='switchSheet(").append(i).append(",").append(sheetCount).append(")'>")
                        .append(Strings.escapeHtml(workbook.getSheetName(i))).append("</button>");
            }
            sb.append("</div>");

            for (int i = 0; i < sheetCount; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                sb.append("<div id='sheet-").append(i).append("' class='sheet-container' style='display:").append(i == 0 ? "block" : "none").append(";'>");
                sb.append("<table class='excel-table'>");
                int rows = Math.min(sheet.getLastRowNum(), 150);
                for (int r = 0; r <= rows; r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;
                    sb.append("<tr>");
                    int cols = Math.min(row.getLastCellNum(), 40);
                    for (int c = 0; c < cols; c++) {
                        String val = new DataFormatter().formatCellValue(row.getCell(c));
                        if (r == 0) {
                            sb.append("<th title='").append(Strings.escapeHtml(val)).append("'>").append(Strings.escapeHtml(val)).append("</th>");
                        } else {
                            sb.append("<td title='").append(Strings.escapeHtml(val)).append("'><div class='excel-cell-truncate'>")
                                    .append(Strings.escapeHtml(val)).append("</div></td>");
                        }
                    }
                    sb.append("</tr>");
                }
                sb.append("</table></div>");
            }
        } catch (Exception e) { return "<div>Excel Error: " + e.getMessage() + "</div>"; }
        return sb.toString();
    }

    /**
     * 辅助：获取单元格内容并格式化
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter(); // POI 提供的格式化工具，能较好处理数字/日期格式
        return formatter.formatCellValue(cell);
    }

    public static String generateWordPreviewHtml(File file, String url) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='padding:20px; font-family: sans-serif; max-width: 800px; margin: 0 auto;'>");

        // 1. 顶部文件信息
        sb.append("<div style='margin-bottom:20px; border-bottom: 2px solid #eee; padding-bottom: 10px;'>")
                .append("<span style='font-size:20px; font-weight:bold; color:#333;'>内容预览</span>")
                .append("<span style='margin-left:10px; color:#888; font-size:13px;'>").append(file.getName()).append("</span>")
                .append("</div>");

        // 2. 提取文本内容
        sb.append("<div style='position:relative; line-height:1.8; color:#444; font-size:15px;'>");
        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
            String text = "";
            String suffix = org.nutz.lang.Files.getSuffixName(file).toLowerCase();

            if ("docx".equals(suffix)) {
                try (org.apache.poi.xwpf.usermodel.XWPFDocument doc = new org.apache.poi.xwpf.usermodel.XWPFDocument(fis)) {
                    java.util.List<org.apache.poi.xwpf.usermodel.XWPFParagraph> paragraphs = doc.getParagraphs();
                    // 仅提取前 10 段
                    for (int i = 0; i < Math.min(paragraphs.size(), 10); i++) {
                        text += "<p>" + Strings.escapeHtml(paragraphs.get(i).getText()) + "</p>";
                    }
                }
            } else if ("doc".equals(suffix)) {
                try (org.apache.poi.hwpf.HWPFDocument doc = new org.apache.poi.hwpf.HWPFDocument(fis)) {
                    org.apache.poi.hwpf.extractor.WordExtractor extractor = new org.apache.poi.hwpf.extractor.WordExtractor(doc);
                    String[] p = extractor.getParagraphText();
                    for (int i = 0; i < Math.min(p.length, 10); i++) {
                        text += "<p>" + Strings.escapeHtml(p[i]) + "</p>";
                    }
                }
            }

            if (Strings.isBlank(text)) {
                sb.append("<p style='color:#999; font-style:italic;'>无法提取文本内容或文档为空</p>");
            } else {
                sb.append(text);
            }
        } catch (Exception e) {
            sb.append("<p style='color:red;'>预览提取失败: ").append(e.getMessage()).append("</p>");
        }

        // 3. 渐变遮罩效果（暗示内容未完）
        sb.append("<div style='position:absolute; bottom:0; left:0; width:100%; height:100px; ")
                .append("background: linear-gradient(transparent, white);'></div>");
        sb.append("</div>");

        // 4. 下载引导区
        sb.append("<div style='text-align:center; margin-top:30px; padding:30px; border:1px dashed #ddd; border-radius:8px; background:#fcfcfc;'>")
                .append("<div style='margin-bottom:15px; color:#666;'>由于 Word 排版复杂，在线仅展示文本摘要</div>")
                .append("<a href='").append(url).append("' style='")
                .append("display:inline-block; background:#007bff; color:white; padding:12px 30px; ")
                .append("text-decoration:none; border-radius:5px; font-weight:bold; box-shadow: 0 4px 6px rgba(0,123,255,0.2);")
                .append("'>下载完整文档</a>")
                .append("</div>");

        sb.append("</div>");
        return sb.toString();
    }

    public static String generatePptPreviewHtml(File file, String url) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='padding:20px; font-family: sans-serif; max-width: 800px; margin: 0 auto;'>");

        // 1. 头部信息
        sb.append("<div style='margin-bottom:20px; border-bottom: 2px solid #ff5722; padding-bottom: 10px;'>")
                .append("<span style='font-size:20px; font-weight:bold; color:#333;'>幻灯片大纲预览</span>")
                .append("<span style='margin-left:10px; color:#888; font-size:13px;'>").append(file.getName()).append("</span>")
                .append("</div>");

        // 2. 提取文本内容
        sb.append("<div style='position:relative;'>");
        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
            String suffix = org.nutz.lang.Files.getSuffixName(file).toLowerCase();

            if ("pptx".equals(suffix)) {
                try (org.apache.poi.xslf.usermodel.XMLSlideShow ppt = new org.apache.poi.xslf.usermodel.XMLSlideShow(fis)) {
                    java.util.List<org.apache.poi.xslf.usermodel.XSLFSlide> slides = ppt.getSlides();
                    int maxSlides = Math.min(slides.size(), 8); // 预览前 8 页

                    for (int i = 0; i < maxSlides; i++) {
                        org.apache.poi.xslf.usermodel.XSLFSlide slide = slides.get(i);
                        sb.append("<div style='margin-bottom:15px; padding:10px; background:#f9f9f9; border-left:4px solid #ff5722;'>");
                        sb.append("<div style='font-weight:bold; color:#ff5722; font-size:12px; margin-bottom:5px;'>Slide ").append(i + 1).append("</div>");

                        // 提取该页所有文本框内容
                        for (org.apache.poi.xslf.usermodel.XSLFShape shape : slide.getShapes()) {
                            if (shape instanceof org.apache.poi.xslf.usermodel.XSLFTextShape) {
                                String txt = ((org.apache.poi.xslf.usermodel.XSLFTextShape) shape).getText();
                                if (Strings.isNotBlank(txt)) {
                                    sb.append("<div style='font-size:14px; color:#444; margin-bottom:3px;'>")
                                            .append(Strings.escapeHtml(txt)).append("</div>");
                                }
                            }
                        }
                        sb.append("</div>");
                    }
                }
            } else {
                sb.append("<p style='color:#999; font-style:italic;'>暂不支持旧版 .ppt 格式的摘要提取</p>");
            }
        } catch (Exception e) {
            sb.append("<p style='color:red;'>PPT 预览提取失败: ").append(e.getMessage()).append("</p>");
        }

        // 3. 渐变遮罩
        sb.append("<div style='position:absolute; bottom:0; left:0; width:100%; height:100px; ")
                .append("background: linear-gradient(transparent, white);'></div>");
        sb.append("</div>");

        // 4. 下载按钮
        sb.append("<div style='text-align:center; margin-top:30px; padding:30px; border:1px dashed #ddd; border-radius:8px; background:#fffcfb;'>")
                .append("<div style='margin-bottom:15px; color:#666;'>由于 PPT 包含大量图形，在线仅展示文字大纲</div>")
                .append("<a href='").append(url).append("' style='")
                .append("display:inline-block; background:#ff5722; color:white; padding:12px 30px; ")
                .append("text-decoration:none; border-radius:5px; font-weight:bold; box-shadow: 0 4px 6px rgba(255,87,34,0.2);")
                .append("'>下载完整演示文稿</a>")
                .append("</div>");

        sb.append("</div>");
        return sb.toString();
    }
}
