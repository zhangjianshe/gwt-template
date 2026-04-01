package cn.mapway.gwt_template.client.workspace.wiki;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.shared.db.DevProjectPageCommitEntity;
import cn.mapway.gwt_template.shared.rpc.project.wiki.QueryPageCommitsRequest;
import cn.mapway.gwt_template.shared.rpc.project.wiki.QueryPageCommitsResponse;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

import java.util.List;

public class WikiHistoryTimeline extends CommonEventComposite {
    private static final WikiHistoryTimelineUiBinder ourUiBinder = GWT.create(WikiHistoryTimelineUiBinder.class);
    @UiField
    FlowPanel root;
    @UiField
    SStyle style;
    private FlowPanel lastSelectedWidget = null;

    public WikiHistoryTimeline() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public void loadPageHistory(String pageId) {
        QueryPageCommitsRequest request = new QueryPageCommitsRequest();
        request.setPageId(pageId);
        AppProxy.get().queryPageCommits(request, new AsyncAdaptor<RpcResult<QueryPageCommitsResponse>>() {
            @Override
            public void onData(RpcResult<QueryPageCommitsResponse> result) {
                render(result.getData().getCommits());
            }
        });
    }

    /**
     * 渲染提交记录列表
     */
    public void render(List<DevProjectPageCommitEntity> commits) {
        root.clear();
        if (commits == null || commits.isEmpty()) {
            root.add(new HTML("<div class='no-data'>暂无修订记录</div>"));
            return;
        }

        for (DevProjectPageCommitEntity commit : commits) {
            root.add(createTimelineItem(commit));
        }
    }

    private FlowPanel createTimelineItem(DevProjectPageCommitEntity commit) {
        FlowPanel item = new FlowPanel();
        item.setStyleName(style.timelineItem());

        // 1. 创建 Marker
        FlowPanel marker = new FlowPanel();
        marker.setStyleName(style.timelineMarker());
        item.add(marker);

        // 2. 创建 Content 容器
        FlowPanel content = new FlowPanel();
        content.setStyleName(style.timelineContent());

        // 3. 构建 Info 行 (头像 + 名字 + 时间)
        String infoHtml = "<img src='" + commit.getAuthorAvatar() + "' class='" + style.avatarSm() + "' />" +
                "<span class='" + style.author() + "'>" + commit.getAuthorName() + "</span>" + // 注意：SStyle 需要增加 author()
                "<span class='" + style.time() + "'>" + formatTime(commit.getCreateTime()) + "</span>";
        content.add(new HTML(infoHtml));

        // 4. 消息和 ID
        HTML msg = new HTML(commit.getMessage());
        msg.setStyleName(style.message());
        content.add(msg);

        HTML cid = new HTML("#" + commit.getId().substring(0, 8));
        cid.setStyleName(style.commitId());
        content.add(cid);

        item.add(content);

        // 绑定点击事件 (保持不变)
        item.addDomHandler(event -> {
            // 清除上一个选中的样式
            if (lastSelectedWidget != null) {
                lastSelectedWidget.removeStyleName(style.active());
            }
            // 设置当前选中
            item.addStyleName(style.active());
            lastSelectedWidget = item;

            // 触发业务事件
            fireEvent(CommonEvent.selectEvent(commit));
        }, ClickEvent.getType());

        return item;
    }

    private String formatTime(java.util.Date date) {
        return StringUtil.formatDate(date);
    }

    interface WikiHistoryTimelineUiBinder extends UiBinder<FlowPanel, WikiHistoryTimeline> {
    }

    interface SStyle extends CssResource {

        @ClassName("timeline-marker")
        String timelineMarker();

        @ClassName("wiki-timeline")
        String wikiTimeline();

        @ClassName("timeline-info")
        String timelineInfo();

        @ClassName("timeline-content")
        String timelineContent();

        @ClassName("avatar-sm")
        String avatarSm();

        String message();

        @ClassName("commit-id")
        String commitId();

        @ClassName("timeline-item")
        String timelineItem();

        String author();

        String time();

        String active();
    }
}