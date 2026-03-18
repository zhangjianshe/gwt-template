package cn.mapway.gwt_template.client.widget.file;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;

/**
 * ProgressLabel
 * 能够展示进度条的标签
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
public class ProgressLabel extends LayoutPanel {

    Label text;
    Label background;

    public ProgressLabel() {
        super();
        text = new Label();
        background = new Label();
        add(background);
        add(text);
        Style style = background.getElement().getStyle();
        style.setWidth(0, Style.Unit.PCT);
        style.setBackgroundColor("skyblue");

        style = text.getElement().getStyle();
        style.setTextAlign(Style.TextAlign.RIGHT);
        style.setOverflow(Style.Overflow.HIDDEN);
        style.setTextOverflow(Style.TextOverflow.ELLIPSIS);
        style.setVerticalAlign(Style.VerticalAlign.MIDDLE);
    }



    public void setText(String text) {
        this.text.setText(text);
    }

    public void setTextAlign(Style.TextAlign textAlign) {
        text.getElement().getStyle().setTextAlign(textAlign);
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        Scheduler.get().scheduleDeferred(() -> {
            int height1 = getElement().getOffsetHeight();
            text.getElement().getStyle().setLineHeight(height1, Style.Unit.PX);
        });
    }

    public void setProgress(int progress) {
        if (progress <= 0) {
            progress = 0;
        } else if (progress >= 100) {
            progress = 100;
        }
        background.getElement().getStyle().setWidth(progress, Style.Unit.PCT);
    }
}
