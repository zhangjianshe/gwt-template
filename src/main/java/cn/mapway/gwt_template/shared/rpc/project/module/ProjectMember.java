package cn.mapway.gwt_template.shared.rpc.project.module;

import cn.mapway.document.annotation.ApiField;
import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 项目成员信息 (合并了用户信息与成员权限)
 * 用于前端列表展示和权限管理
 */
@Data
@Doc("工作空间成员信息")
public class ProjectMember implements Serializable, IsSerializable {

    // --- 用户基础资料 (来自 rbac_user) ---
    @ApiField("用户ID")
    private Long userId;

    @ApiField("登录账号")
    private String userName;

    @ApiField("显示昵称")
    private String nickName;

    @ApiField("头像地址")
    private String avatar;

    @ApiField("电子邮箱")
    private String email;

    // --- 成员权限资料 (来自 dev_workspace_member) ---
    @ApiField("工作空间ID")
    private String workspaceId;

    @ApiField("项目ID")
    private String projectId;

    @ApiField("小组ID")
    private String teamId;

    @ApiField("加入时间")
    private Date createTime;

    @ApiField("是否为所有者")
    private Boolean isOwner;

    @ApiField("权限位")
    private Integer permission;
}