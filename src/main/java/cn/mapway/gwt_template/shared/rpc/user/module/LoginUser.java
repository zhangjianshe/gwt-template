package cn.mapway.gwt_template.shared.rpc.user.module;

import cn.mapway.document.annotation.ApiField;
import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Doc("登录用户信息")
public class LoginUser implements Serializable, IsSerializable {

    @ApiField(value = "服务器时间", example = "2024-10-01 12:32:13")
    String serverTime;
}
