package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectIssueCommentEntity;
import cn.mapway.gwt_template.shared.db.DevProjectIssueEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectIssueCommentRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectIssueCommentResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.IssueCommentKind;
import cn.mapway.gwt_template.shared.rpc.project.module.IssueState;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.trans.Trans;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * UpdateProjectIssueCommentExecutor
 * 回复一个问题
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateProjectIssueCommentExecutor extends AbstractBizExecutor<UpdateProjectIssueCommentResponse, UpdateProjectIssueCommentRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<UpdateProjectIssueCommentResponse> process(BizContext context, BizRequest<UpdateProjectIssueCommentRequest> bizParam) {
        UpdateProjectIssueCommentRequest request = bizParam.getData();
        log.info("UpdateProjectIssueCommentExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        Long currentOperator = user.getUser().getUserId();
        DevProjectIssueCommentEntity comment = request.getComment();
        assertNotNull(comment, "没有评论数据");
        IssueCommentKind kind = IssueCommentKind.fromCode(comment.getKind());
        comment.setKind(kind.getCode());

        assertTrue(Strings.isNotBlank(comment.getIssueId()), "评论没有问题ID");
        DevProjectIssueEntity issue = dao.fetch(DevProjectIssueEntity.class, comment.getIssueId());
        assertNotNull(issue, "没有找到ISSUE");

        boolean canOperator = currentOperator.equals(issue.getCreateUserId()) || currentOperator.equals(issue.getCharger());
        assertTrue(canOperator, "没有操作权限");

        if (comment.getAttachments() == null) {
            comment.setAttachments(new ArrayList<>());
        }

        comment.setCreateTime(new Timestamp(System.currentTimeMillis()));
        comment.setCreateUserId(currentOperator);
        comment.setUserName(user.getUserName());
        comment.setUserAvatar(user.getAvatar());
        comment.setId(R.UU16());
        comment.setProjectId(issue.getProjectId());
        UpdateProjectIssueCommentResponse response = new UpdateProjectIssueCommentResponse();
        switch (kind) {
            case ICK_COMMENT:
                if (!comment.getAttachments().isEmpty()) {
                    if (Strings.isBlank(comment.getContent())) {
                        comment.setContent(user.getUserName() + "上传了附件" + comment.getAttachments().size());
                    }
                } else {
                    assertTrue(Strings.isNotBlank(comment.getContent()), "没有评论内容");
                }
                dao.insert(comment);
                response.setComment(comment);
                response.setUpdateIssue(false);
                break;
            case ICK_CLOSE:
                String closeReason = "";
                if (Strings.isNotBlank(comment.getContent())) {
                    closeReason = comment.getContent();
                }
                comment.setContent(user.getUserName() + "关闭了问题" + "\r\n> " + closeReason);
                Trans.exec(() -> {
                    closeIssue(comment.getIssueId());
                    dao.insert(comment);
                });
                response.setComment(comment);
                response.setUpdateIssue(true);
                break;
            case ICK_REPOEN:
                comment.setContent(user.getUserName() + "重新打开了问题");
                Trans.exec(() -> {
                    openIssue(comment.getIssueId());
                    dao.insert(comment);
                });
                response.setComment(comment);
                response.setUpdateIssue(true);
                break;
            case ICK_REASSIGN: {
                // content is the target user's userId
                try {
                    Long targetUserId = Long.parseLong(comment.getContent());
                    RbacUserEntity targetUser = dao.fetch(RbacUserEntity.class, targetUserId);
                    if (targetUser == null) {
                        return BizResult.error(500, "没有找到目标用户");
                    }
                    comment.setContent(user.getUserName() + "转移了任务给" + targetUser.getUserName());
                    dao.insert(comment);
                    resignTargetUser(comment.getIssueId(), targetUserId);
                    response.setComment(comment);
                    response.setUpdateIssue(true);
                } catch (Exception e) {
                    return BizResult.error(500, e.getMessage());
                }
                break;
            }
            default:
                assertTrue(false, "不支持的评论类型");

        }


        return BizResult.success(response);
    }

    private void resignTargetUser(String issueId, Long targetUserId) {
        DevProjectIssueEntity issue = new DevProjectIssueEntity();
        issue.setId(issueId);
        issue.setCharger(targetUserId);
        dao.updateIgnoreNull(issue);
    }

    private void closeIssue(String issueId) {
        DevProjectIssueEntity issue = new DevProjectIssueEntity();
        issue.setId(issueId);
        issue.setState(IssueState.IS_CLOSED.getCode());
        dao.updateIgnoreNull(issue);
    }

    private void openIssue(String issueId) {
        DevProjectIssueEntity issue = new DevProjectIssueEntity();
        issue.setId(issueId);
        issue.setState(IssueState.IS_OPEN.getCode());
        dao.updateIgnoreNull(issue);
    }
}
