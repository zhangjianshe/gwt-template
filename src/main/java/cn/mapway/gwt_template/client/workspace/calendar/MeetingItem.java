package cn.mapway.gwt_template.client.workspace.calendar;

import cn.mapway.gwt_template.client.js.markdown.ConvertOptions;
import cn.mapway.gwt_template.client.js.markdown.MarkdownConvert;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskPriority;
import cn.mapway.ui.client.mvc.attribute.DataCastor;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.tools.JSON;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

public class MeetingItem extends CommonEventComposite implements IData<DevProjectTaskEntity> {
    private static final MeetingItemUiBinder ourUiBinder = GWT.create(MeetingItemUiBinder.class);
    @UiField
    HTML lbContent;
    @UiField
    Label lbTime;
    @UiField
    Label lbTitle;
    @UiField
    Label lbParticipant;
    @UiField
    Label lbLocation;
    @UiField
    Label lbPriority;
    @UiField
    SimplePanel circleDot;
    @UiField
    Anchor btnEdit;
    MarkdownConvert convert;
    private DevProjectTaskEntity meeting;

    public MeetingItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
        ConvertOptions convertOptions = ConvertOptions.create();
        convertOptions.tables = true;
        convert = new MarkdownConvert(convertOptions);
    }


    @Override
    public DevProjectTaskEntity getData() {
        return meeting;
    }

    @Override
    public void setData(DevProjectTaskEntity obj) {
        meeting = obj;
        toUI();
    }

    private void toUI() {
        lbTitle.setText(meeting.getName());
        lbTime.setText(StringUtil.formatDate(meeting.getStartTime()));
        lbContent.setHTML(meeting.getSummary());

        DevTaskPriority priority = DevTaskPriority.fromCode(meeting.getPriority());
        lbPriority.setText(priority.getName());
        lbPriority.getElement().getStyle().setColor(priority.getColor());
        circleDot.getElement().getStyle().setBackgroundColor(priority.getColor());

        String jsonStr = meeting.getSummary();
        Object json = JSON.parse(jsonStr);
        JsPropertyMap mapper = Js.asPropertyMap(json);

        lbParticipant.setText("👥 " + mapper.get("participant")); // 结果：👥 好多人
        lbLocation.setText("📍 " + mapper.get("location"));       // 结果：📍 这里是北京
        String markdown = DataCastor.castToString(mapper.get("body"));
        lbContent.setHTML(convert.makeHtml(markdown));                     // 结果：法学纪律
    }

    @UiHandler("btnEdit")
    public void btnEditClick(ClickEvent event) {
        fireEvent(CommonEvent.editEvent(meeting));
    }

    interface MeetingItemUiBinder extends UiBinder<HTMLPanel, MeetingItem> {
    }
}