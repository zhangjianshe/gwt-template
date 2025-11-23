package cn.mapway.gwt_template.client.resource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface AppResource extends ClientBundle {
    public static final AppResource INSTANCE = GWT.create(AppResource.class);
    @Source("./images/delete.png")
    ImageResource delete();
}
