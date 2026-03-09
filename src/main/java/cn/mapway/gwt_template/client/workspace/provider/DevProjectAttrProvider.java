package cn.mapway.gwt_template.client.workspace.provider;

import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.ui.client.mvc.attribute.AbstractAttribute;
import cn.mapway.ui.client.mvc.attribute.AbstractAttributesProvider;
import cn.mapway.ui.client.mvc.attribute.DataCastor;
import cn.mapway.ui.client.mvc.attribute.IAttribute;
import cn.mapway.ui.client.mvc.attribute.atts.ColorBoxAttribute;
import cn.mapway.ui.client.mvc.attribute.atts.ImageUploadBoxAttribute;
import cn.mapway.ui.client.mvc.attribute.atts.TextBoxAttribute;
import cn.mapway.ui.client.mvc.attribute.design.IEditorMetaData;
import cn.mapway.ui.client.mvc.attribute.editor.ParameterKeys;
import cn.mapway.ui.client.mvc.attribute.editor.icon.IconSelectorEditorMetaData;
import cn.mapway.ui.client.mvc.attribute.editor.sys.TextAreaAttribute;
import com.google.gwt.core.client.GWT;

import java.util.List;

public class DevProjectAttrProvider extends AbstractAttributesProvider {
    DevProjectEntity project;

    public void rebuild(DevProjectEntity project) {
        this.project = project;
        List<IAttribute> attributes = getAttributes();
        attributes.clear();
        if (project == null) {
            notifyAttributeReady();
            return;
        }
        attributes.add(new TextBoxAttribute("name", "名称") {
            @Override
            public Object getValue() {
                return project.getName();
            }

            @Override
            public void setValue(Object value) {
                project.setName(DataCastor.castToString(value));
            }
        });
        attributes.add(new ColorBoxAttribute("color", "颜色") {
            @Override
            public Object getValue() {
                return project.getColor();
            }

            @Override
            public void setValue(Object value) {
                project.setColor(DataCastor.castToString(value));
            }
        });
        attributes.add(new ImageUploadBoxAttribute("icon", "图片") {
            @Override
            public Object getValue() {
                return project.getIcon();
            }

            @Override
            public void setValue(Object value) {
                project.setIcon(DataCastor.castToString(value));
            }
        }.param(ParameterKeys.KEY_HEIGHT, "150px")
                .param(ParameterKeys.KEY_IMAGE_UPLOAD_ACTION, GWT.getHostPageBaseURL() + "fileUpload")
                .param(ParameterKeys.KEY_IMAGE_UPLOAD_REL, "project"));

        attributes.add(new TextAreaAttribute("summary", "介绍") {
            @Override
            public Object getValue() {
                return project.getSummary();
            }

            @Override
            public void setValue(Object value) {
                project.setSummary(DataCastor.castToString(value));
            }
        });
        attributes.add(new AbstractAttribute("unicode", "图标") {
            @Override
            public IEditorMetaData getEditorMetaData() {
                return new IconSelectorEditorMetaData();
            }

            @Override
            public Object getValue() {
                return project.getUnicode();
            }

            @Override
            public void setValue(Object value) {
                project.setUnicode(DataCastor.castToString(value));
            }
        });
        notifyAttributeReady();
    }

    @Override
    public String getAttributeTitle() {
        if (project == null) {
            return "没有目标";
        } else {
            return project.getName();
        }
    }

    public DevProjectEntity getData() {
        return project;
    }
}
