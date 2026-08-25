package cn.mapway.gwt_template.client.dns.powerdns;

import cn.mapway.gwt_template.shared.rpc.powerdns.model.PowerDnsRRSet;
import cn.mapway.gwt_template.shared.rpc.powerdns.model.PowerDnsRecord;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.mvc.attribute.DataCastor;
import cn.mapway.ui.client.widget.AiTextBox;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.List;

public class PowerDnsRecordEditor extends CommonEventComposite {
    interface PowerDnsRecordEditorUiBinder extends UiBinder<DockLayoutPanel, PowerDnsRecordEditor> {
    }

    private static final PowerDnsRecordEditorUiBinder ourUiBinder = GWT.create(PowerDnsRecordEditorUiBinder.class);

    @UiField
    AiTextBox txtName;
    @UiField
    ListBox lstType;
    @UiField
    IntegerBox txtTtl;
    @UiField
    TextArea txtContent;
    @UiField
    SaveBar saveBar;

    private PowerDnsRRSet rrset;

    private static Dialog<PowerDnsRecordEditor> dialog;

    public static Dialog<PowerDnsRecordEditor> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<PowerDnsRecordEditor> createOne() {
        PowerDnsRecordEditor editor = new PowerDnsRecordEditor();
        return new Dialog<>(editor, "DNS编辑");
    }

    public PowerDnsRecordEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
        initTypes();
    }

    private void initTypes() {
        String[] types = {"A", "AAAA", "CNAME", "MX", "TXT", "NS", "SRV", "SOA", "PTR"};
        for (String type : types) {
            lstType.addItem(type);
        }
    }

    /**
     * 设置数据
     *
     * @param data 为空表示新增，不为空表示编辑
     */
    public void setData(PowerDnsRRSet data) {
        this.rrset = data;
        if (data != null) {
            txtName.setText(data.getName());
            txtTtl.setValue(DataCastor.castToInteger(data.getTtl()));
            selectType(data.getType());

            // 将多个 record 的 content 合并到 TextArea
            StringBuilder sb = new StringBuilder();
            if (data.getRecords() != null) {
                for (PowerDnsRecord r : data.getRecords()) {
                    sb.append(r.getContent()).append("\n");
                }
            }
            txtContent.setText(sb.toString().trim());
            // 编辑模式下通常不允许修改 Name 和 Type (PowerDNS API 限制)
            txtName.setEnabled(false);
            lstType.setEnabled(false);
        } else {
            txtName.setText("");
            txtTtl.setValue(3600);
            txtContent.setText("");
            txtName.setEnabled(true);
            lstType.setEnabled(true);
        }
    }

    private void selectType(String type) {
        for (int i = 0; i < lstType.getItemCount(); i++) {
            if (lstType.getItemText(i).equalsIgnoreCase(type)) {
                lstType.setSelectedIndex(i);
                break;
            }
        }
    }

    /**
     * 获取更新后的 RRSet 对象
     */
    public PowerDnsRRSet getData() {
        PowerDnsRRSet result = (rrset != null) ? rrset : new PowerDnsRRSet();
        result.setName(txtName.getText());
        result.setType(lstType.getSelectedValue());
        result.setTtl(DataCastor.castToLong(txtTtl.getValue()));

        // 解析 TextArea 中的多行内容
        String[] lines = txtContent.getText().split("\n");
        List<PowerDnsRecord> records = new ArrayList<>();
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                PowerDnsRecord r = new PowerDnsRecord();
                r.setContent(line.trim());
                r.setDisabled(false);
                records.add(r);
            }
        }
        result.setRecords(records);
        result.setChangetype("REPLACE"); // 告知 PowerDNS 覆盖现有记录
        return result;
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(800, 530);
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            fireEvent(CommonEvent.okEvent(getData()));
        } else {
            fireEvent(event);
        }
    }

}