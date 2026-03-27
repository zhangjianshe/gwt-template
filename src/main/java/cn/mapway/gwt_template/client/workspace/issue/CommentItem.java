package cn.mapway.gwt_template.client.workspace.issue;

import cn.mapway.gwt_template.client.js.markdown.MarkdownConvert;
import cn.mapway.gwt_template.shared.db.DevProjectIssueCommentEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.IssueCommentKind;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

public class CommentItem extends Composite implements IData<DevProjectIssueCommentEntity> {
    private static final CommonItemUiBinder ourUiBinder = GWT.create(CommonItemUiBinder.class);
    private final static MarkdownConvert convert = new MarkdownConvert();
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
    @UiField
    SStyle style;
    private DevProjectIssueCommentEntity comment;

    public CommentItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
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
        // 1. 处理时间格式化
        String timeStr = "";
        if (comment.getCreateTime() != null) {
            long delta = System.currentTimeMillis() - comment.getCreateTime().getTime();
            timeStr = StringUtil.formatTimeSpan(delta / 1000);
        }

        // 2. 根据类型切换样式
        IssueCommentKind kind = IssueCommentKind.fromCode(comment.getKind());
        boolean isSystemAction = kind != null && kind != IssueCommentKind.ICK_COMMENT;

        if (isSystemAction) {
            // 系统操作：隐藏头像，切换到淡色样式
            mainPanel.addStyleName(style.systemLog());
            pnlAvatar.setVisible(false);
            pnlHeader.setVisible(false); // 系统操作通常只有一行字

            String actionText = formatActionText(kind, comment);
            htmlContent.setHTML("<span class='" + style.systemIcon() + "'>•</span> " +
                    "<b>" + comment.getUserName() + "</b> " + actionText + " " + timeStr);
        } else {
            // 普通评论：恢复默认样式
            mainPanel.removeStyleName(style.systemLog());
            pnlAvatar.setVisible(true);
            pnlHeader.setVisible(true);

            lbUser.setText(comment.getUserName());
            lbDate.setText(" 评论 " + timeStr);
            if (StringUtil.isNotBlank(comment.getUserAvatar())) {
                imgAvatar.setUrl(comment.getUserAvatar());
            }
            String rawContent = comment.getContent();
            htmlContent.setHTML(StringUtil.isBlank(rawContent) ?
                    "<i style='color:#999'>No description.</i>" : convert.makeHtml(rawContent));
        }
    }

    /**
     * 将系统动作代码转换为可读文字
     */
    private String formatActionText(IssueCommentKind kind, DevProjectIssueCommentEntity comment) {
        switch (kind) {
            case ICK_REASSIGN:
                return "重新分配任务给" + comment.getContent();
            case ICK_CLOSE:
                return "关闭了问题";
            case ICK_REPOEN:
                return "重新打开了问题";
            default:
                return "performed an action";
        }
    }

    interface SStyle extends CssResource {

        @ClassName("item-container")
        String itemContainer();

        String date();

        @ClassName("avatar-area")
        String avatarArea();

        @ClassName("user-name")
        String userName();

        String header();

        @ClassName("system-log")
        String systemLog();

        @ClassName("content-area")
        String contentArea();

        String avatar();

        String body();

        @ClassName("system-icon")
        String systemIcon();
    }

    interface CommonItemUiBinder extends UiBinder<HTMLPanel, CommentItem> {
    }
}