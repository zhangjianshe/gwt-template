package cn.mapway.gwt_template.shared.rpc.project.module;

import lombok.Getter;

public enum IssueCommentKind {
    ICK_COMMENT(0, "评论"),
    ICK_REASSIGN(1, "转移任务"),
    ICK_REPOEN(2, "重新打开"),
    ICK_CLOSE(3, "关闭任务");

    @Getter
    final Integer code;
    @Getter
    final
    String desc;

    IssueCommentKind(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static IssueCommentKind fromCode(Integer code) {
        if (code == null) {
            return ICK_COMMENT;
        }
        for (IssueCommentKind issueCommentKind : IssueCommentKind.values()) {
            if (issueCommentKind.code == code) {
                return issueCommentKind;
            }
        }
        return ICK_COMMENT;
    }
}
