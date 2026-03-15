package cn.mapway.gwt_template.client.resource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface AppResource extends ClientBundle {
    AppResource INSTANCE = GWT.create(AppResource.class);

    @Source("./images/delete.png")
    ImageResource delete();

    @Source("./images/info.png")
    ImageResource info();

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
