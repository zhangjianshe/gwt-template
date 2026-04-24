package cn.mapway.gwt_template.client.resource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface AppResource extends ClientBundle {
    AppResource INSTANCE = GWT.create(AppResource.class);

    @Source("./images/warning.png")
    ImageResource warning();
    // --- 状态图标 ---
    @Source("./images/issue_opened.png")
    ImageResource statusOpen();

    @Source("./images/issue_closed.png")
    ImageResource statusClosed();

    @Source("./images/issue_created.png")
    ImageResource statusCreated();

    // --- 优先级图标 ---
    @Source("./images/high_priority.png")
    ImageResource priorityHigh();

    @Source("./images/medium_priority.png")
    ImageResource priorityMedium();

    @Source("./images/low_priority.png")
    ImageResource priorityLow();

    @Source("./images/delete.png")
    ImageResource delete();

    @Source("./images/info.png")
    ImageResource info();

    @Source("./images/icon-right.png")
    ImageResource rightArrow();

    @Source("app.css")
    AppCss styles();

    @Source("./images/avatar.png")
    ImageResource avatar();

    @Source("./images/emptyAvatar.png")
    ImageResource emptyAvatar();

    @Source("./images/noData.png")
    ImageResource noData();

    @Source("./images/noFindProject.png")
    ImageResource noFindProject();
}
