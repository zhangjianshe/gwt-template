package cn.mapway.gwt_template.client.workspace.provider;

import cn.mapway.gwt_template.shared.db.DevWorkspaceEntity;
import cn.mapway.ui.client.mvc.attribute.AbstractAttribute;
import cn.mapway.ui.client.mvc.attribute.AbstractAttributesProvider;
import cn.mapway.ui.client.mvc.attribute.DataCastor;
import cn.mapway.ui.client.mvc.attribute.IAttribute;
import cn.mapway.ui.client.mvc.attribute.atts.CheckBoxAttribute;
import cn.mapway.ui.client.mvc.attribute.atts.ColorBoxAttribute;
import cn.mapway.ui.client.mvc.attribute.atts.ImageUploadBoxAttribute;
import cn.mapway.ui.client.mvc.attribute.atts.TextBoxAttribute;
import cn.mapway.ui.client.mvc.attribute.design.IEditorMetaData;
import cn.mapway.ui.client.mvc.attribute.editor.ParameterKeys;
import cn.mapway.ui.client.mvc.attribute.editor.icon.IconSelectorEditorMetaData;
import cn.mapway.ui.client.mvc.attribute.editor.sys.TextAreaAttribute;
import com.google.gwt.core.client.GWT;

import java.util.List;

public class WorkspaceAttrProvider extends AbstractAttributesProvider {
    DevWorkspaceEntity workspace;

    public void rebuild(DevWorkspaceEntity workspace) {
        this.workspace = workspace;
        List<IAttribute> attributes = getAttributes();
        attributes.clear();
        if (workspace == null) {
            notifyAttributeReady();
            return;
        }
        attributes.add(new TextBoxAttribute("name", "名称") {
            @Override
            public Object getValue() {
                return workspace.getName();
            }

            @Override
            public void setValue(Object value) {
                workspace.setName(DataCastor.castToString(value));
            }
        });
        attributes.add(new ColorBoxAttribute("color", "颜色") {
            @Override
            public Object getValue() {
                return workspace.getColor();
            }

            @Override
            public void setValue(Object value) {
                workspace.setColor(DataCastor.castToString(value));
            }
        });
        attributes.add(new ImageUploadBoxAttribute("icon", "图片") {
            @Override
            public Object getValue() {
                return workspace.getIcon();
            }

            @Override
            public void setValue(Object value) {
                workspace.setIcon(DataCastor.castToString(value));
            }
        }.param(ParameterKeys.KEY_HEIGHT, "150px")
                .param(ParameterKeys.KEY_IMAGE_UPLOAD_ACTION, GWT.getHostPageBaseURL() + "fileUpload")
                .param(ParameterKeys.KEY_IMAGE_UPLOAD_REL, "workspace"));

        attributes.add(new CheckBoxAttribute("share", "共享") {
            @Override
            public Object getValue() {
                return workspace.getIsShare();
            }

            @Override
            public void setValue(Object value) {
                workspace.setIsShare(DataCastor.castToBoolean(value));
            }
        });
        attributes.add(new TextAreaAttribute("summary", "介绍") {
            @Override
            public Object getValue() {
                return workspace.getSummary();
            }

            @Override
            public void setValue(Object value) {
                workspace.setSummary(DataCastor.castToString(value));
            }
        });
        attributes.add(new AbstractAttribute("unicode", "图标") {
            @Override
            public IEditorMetaData getEditorMetaData() {
                return new IconSelectorEditorMetaData();
            }

            @Override
            public Object getValue() {
                return workspace.getUnicode();
            }

            @Override
            public void setValue(Object value) {
                workspace.setUnicode(DataCastor.castToString(value));
            }
        });
        notifyAttributeReady();
    }

    @Override
    public String getAttributeTitle() {
        if (workspace == null) {
            return "没有目标";
        } else {
            return workspace.getName();
        }
    }

    public DevWorkspaceEntity getData() {
        return workspace;
    }
}
