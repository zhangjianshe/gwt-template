package cn.mapway.gwt_template.client.preference.app;

import cn.mapway.gwt_template.shared.rpc.app.AppData;
import cn.mapway.ui.client.mvc.attribute.AbstractAttributesProvider;
import cn.mapway.ui.client.mvc.attribute.DataCastor;
import cn.mapway.ui.client.mvc.attribute.IAttribute;
import cn.mapway.ui.client.mvc.attribute.atts.ImageUploadBoxAttribute;
import cn.mapway.ui.client.mvc.attribute.editor.ParameterKeys;
import com.google.gwt.core.client.GWT;

import java.util.List;

public class AppConfigAttrProvider extends AbstractAttributesProvider {
    AppData appData;

    public void rebuild(AppData appData) {
        this.appData = appData;
        List<IAttribute> attributes = getAttributes();
        attributes.clear();

        attributes.add(new ImageUploadBoxAttribute("logo", "应用LOGO") {
            @Override
            public Object getValue() {
                return appData.getLogo();
            }

            @Override
            public void setValue(Object value) {
                appData.setLogo(DataCastor.castToString(value));
            }
        }.param(ParameterKeys.KEY_HEIGHT, "150px")
         .param(ParameterKeys.KEY_IMAGE_UPLOAD_ACTION, GWT.getHostPageBaseURL() + "fileUpload")
         .param(ParameterKeys.KEY_IMAGE_UPLOAD_REL, "app"));

        notifyAttributeReady();
    }

    @Override
    public String getAttributeTitle() {
        return "应用信息";
    }
}
