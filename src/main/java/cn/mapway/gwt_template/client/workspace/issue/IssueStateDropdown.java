package cn.mapway.gwt_template.client.workspace.issue;

import cn.mapway.gwt_template.client.workspace.widget.SingleCheck;
import cn.mapway.gwt_template.shared.rpc.project.module.IssueState;

public class IssueStateDropdown extends SingleCheck {
    public IssueStateDropdown() {

    }

    public void init(boolean showNoneValue) {
        for (IssueState state : IssueState.values()) {
            if (!showNoneValue && state.getNoneValue()) {
                continue;
            }
            addItem(state.getName(), state.getCode());
        }
        if (showNoneValue) {
            setValue(IssueState.IS_UNKNOWN.getCode(), false);
        } else {
            setValue(IssueState.IS_OPEN.getCode(), true);
        }
    }
}
