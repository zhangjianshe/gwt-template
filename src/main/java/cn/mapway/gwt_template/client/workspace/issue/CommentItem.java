package cn.mapway.gwt_template.client.workspace.issue;

import cn.mapway.gwt_template.client.js.markdown.MarkdownConvert;
import cn.mapway.gwt_template.shared.db.DevProjectIssueCommentEntity;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

public class CommentItem extends Composite implements IData<DevProjectIssueCommentEntity> {
    private static final CommonItemUiBinder ourUiBinder = GWT.create(CommonItemUiBinder.class);
    private final MarkdownConvert convert;
    @UiField
    Label lbDate;
    @UiField
    Label lbUser;
    @UiField
    HTML htmlContent; // 改用 HTML 控件以支持渲染
    private DevProjectIssueCommentEntity comment;

    public CommentItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
        convert = new MarkdownConvert();
    }

    @Override
    public DevProjectIssueCommentEntity getData() {
        return comment;
    }

    @Override
    public void setData(DevProjectIssueCommentEntity obj) {
        if (obj == null) return;
        this.comment = obj;
        toUI();
    }

    private void toUI() {
        // 渲染用户名和日期
        lbUser.setText(comment.getUserName() == null ? "未知用户" : comment.getUserName());

        if (comment.getCreateTime() != null) {
            long delta = System.currentTimeMillis() - comment.getCreateTime().getTime();
            lbDate.setText(" commented " + StringUtil.formatTimeSpan(delta / 1000));
        }

        // 使用 Markdown 转换内容
        String rawContent = comment.getContent();
        if (rawContent == null || rawContent.isEmpty()) {
            htmlContent.setHTML("<i style='color:#999'>No description provided.</i>");
        } else {
            // 这里调用你的 MarkdownConvert
            htmlContent.setHTML(convert.makeHtml(rawContent));
        }
    }

    interface CommonItemUiBinder extends UiBinder<HTMLPanel, CommentItem> {
    }
}