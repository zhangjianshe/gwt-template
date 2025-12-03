package cn.mapway.gwt_template.server.api;

import cn.mapway.document.annotation.Doc;
import org.springframework.web.bind.annotation.RestController;

@Doc(value = "用户相关",group = "用户")
@RestController("/api/v1/user")
public class UserController extends ApiBaseController{



}
