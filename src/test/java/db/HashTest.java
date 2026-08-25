package db;

import cn.mapway.rbac.shared.RbacConstant;
import org.nutz.lang.Lang;

public class HashTest {
    public static void main(String[] args) {
        String s = Lang.sha1(RbacConstant.SALT + "_imagebot__");
        s = Lang.sha1(RbacConstant.SALT + "_hello__");
        System.out.println(s);
    }
}
