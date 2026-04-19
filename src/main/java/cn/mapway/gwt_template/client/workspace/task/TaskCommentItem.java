package cn.mapway.gwt_template.client.workspace.task;

import cn.mapway.gwt_template.client.js.markdown.MarkdownConvert;
import cn.mapway.gwt_template.shared.db.DevProjectTaskCommentEntity;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

public class TaskCommentItem extends Composite implements IData<DevProjectTaskCommentEntity> {
    private final static MarkdownConvert convert = new MarkdownConvert();
    private static final TaskCommentItemUiBinder ourUiBinder = GWT.create(TaskCommentItemUiBinder.class);
    @UiField
    Label lbDate;
    @UiField
    Label lbUser;
    @UiField
    HTML htmlContent; // 改用 HTML 控件以支持渲染
    @UiField
    HTMLPanel mainPanel;
    @UiField
    HTMLPanel pnlAvatar;
    @UiField
    Image imgAvatar;
    @UiField
    HTMLPanel pnlHeader;
    DevProjectTaskCommentEntity comment;

    public TaskCommentItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public DevProjectTaskCommentEntity getData() {
        return comment;
    }

    @Override
    public void setData(DevProjectTaskCommentEntity obj) {
        comment = obj;
        toUI();
    }

    private void toUI() {
        // 1. 处理时间格式化
        String timeStr = "";
        if (comment.getCreateTime() != null) {
            long delta = System.currentTimeMillis() - comment.getCreateTime().getTime();
            timeStr = StringUtil.formatTimeSpan(delta / 1000);
        }

        // 普通评论：恢复默认样式
        pnlAvatar.setVisible(true);
        pnlHeader.setVisible(true);

        lbUser.setText(comment.getCreateUserName());
        lbDate.setText(" 评论 " + timeStr);
        if (StringUtil.isNotBlank(comment.getCreateUserAvatar())) {
            imgAvatar.setUrl(comment.getCreateUserAvatar());
        }
        String rawContent = comment.getContent();
        htmlContent.setHTML(StringUtil.isBlank(rawContent) ?
                "<i style='color:#999'>No description.</i>" : convert.makeHtml(rawContent));
    }

    interface TaskCommentItemUiBinder extends UiBinder<HTMLPanel, TaskCommentItem> {
    }
}