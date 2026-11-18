package cn.mapway.gwt_template.client.drive;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

interface DriveResources extends ClientBundle {
  DriveResources INSTANCE = GWT.create(DriveResources.class);

  @Source("DriveFrame.css")
  TextResource styles();
}
