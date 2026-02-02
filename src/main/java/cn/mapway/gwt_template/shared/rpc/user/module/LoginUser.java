package cn.mapway.gwt_template.shared.rpc.user.module;

import cn.mapway.document.annotation.ApiField;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.rbac.shared.RbacConstant;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import cn.mapway.ui.client.IUserInfo;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Doc("登录用户信息")
@NoArgsConstructor
public class LoginUser implements Serializable, IsSerializable, IUserInfo {
    @ApiField("用户信息")
    RbacUserEntity user;
    @ApiField(value = "服务器时间", example = "2024-10-01 12:32:13")
    String serverTime;
    @ApiField("登录时间")
    Long loginTime;
    @ApiField("是否是API调用")
    boolean apiInvoke;


    public boolean isAdmin()
    {
        return user!=null && user.getUserId().equals(RbacConstant.SUPER_USER_ID);
    }

    public LoginUser(RbacUserEntity user) {
        this.user = user;
    }

    @Override
    public String getSystemCode() {
        return AppConstant.SYS_CODE;
    }

    @Override
    public String getUserName() {
        return user.getUserName();
    }

    @Override
    public String getNickName() {
        return user.getNickName();
    }

    @Override
    public String getAvatar() {
        return user.getAvatar();
    }

    @Override
    public String getRemark() {
        return user.getRemark();
    }

    @Override
    public Date getUpdateTime() {
        return user.getUpdateTime();
    }

    @Override
    public Date getCreateTime() {
        return user.getCreateTime();
    }

    @Override
    public String getConfig() {
        return user.getConfig();
    }

    @Override
    public String getRelId() {
        return user.getRelId();
    }

    @Override
    public String getSex() {
        return user.getSex();
    }

    @Override
    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public String getUserType() {
        return user.getUserType();
    }

    @Override
    public String getPhone() {
        return user.getPhonenumber();
    }

    @Override
    public String getId() {
        return user.getUserId().toString();
    }

    @Override
    public long getExpireTime() {
        return 0;
    }
}
