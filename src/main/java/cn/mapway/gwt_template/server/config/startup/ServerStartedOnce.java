package cn.mapway.gwt_template.server.config.startup;

import cn.mapway.gwt_template.server.rbac.Permissions;
import cn.mapway.gwt_template.server.service.user.TokenService;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.shared.RbacConstant;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import cn.mapway.server.IServerPlugin;
import cn.mapway.ui.client.IUserInfo;
import cn.mapway.ui.server.IServerContext;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.lang.Strings;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ServerStartedOnce extends ApplicationObjectSupport implements IServerContext, ApplicationListener<ApplicationReadyEvent> {
    ApplicationContext applicationContext;
    Dao dao;
    ConcurrentHashMap<String, ConcurrentHashMap<String, Object>> sessionCacheGroup = new ConcurrentHashMap();
    @Resource
    TokenService tokenService;

    @Override
    public <T> T getBean(Class<T> aClass) {
        return applicationContext.getBean(aClass);
    }

    @Override
    public <T> T getBeanByName(String s) {
        return (T) applicationContext.getBean(s);
    }

    @Override
    public Dao getManagerDao() {
        return dao;
    }

    @Override
    public void putToSession(String group, String key, Object value) {
        if (Strings.isBlank(key)) {
            return;
        }
        if (Strings.isBlank(group)) {
            group = "DEFAULT_GROUP";
        }
        ConcurrentHashMap<String, Object> map = sessionCacheGroup.get(group);
        if (map == null) {
            map = new ConcurrentHashMap<>();
            sessionCacheGroup.put(group, map);
        }
        if (value == null) {
            map.remove(key);
        } else {
            map.put(key, value);
        }
    }

    @Override
    public void clearToSession(String group) {
        if (Strings.isBlank(group)) {
            group = "DEFAULT_GROUP";
        }
        sessionCacheGroup.remove(group);
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
        sessionCacheGroup.put(group, map);
    }


    @Override
    public Object getFromSession(String group, String key) {
        if (Strings.isBlank(key)) {
            return null;
        }
        if (Strings.isBlank(group)) {
            group = "DEFAULT_GROUP";
        }
        ConcurrentHashMap<String, Object> map = sessionCacheGroup.get(group);
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    @Override
    public void clearSessionGroup(String group) {
        if (Strings.isBlank(group)) {
            return;
        }
        sessionCacheGroup.remove(group);
    }

    /**
     * 获取当前的用户
     *
     * @return
     */
    @Override
    public IUserInfo requestUser() {
        return tokenService.requestUser();
    }

    @Override
    public Collection<Class<?>> getScanPackages() {
        Collection<Class<?>> scans = new HashSet<>();
        scans.add(Permissions.class);
        return scans;
    }

    @Override
    public IUserInfo getSuperUser() {
        RbacUserEntity adminUser = dao.fetch(RbacUserEntity.class, Cnd.where(RbacUserEntity.FLD_USER_ID, "=", RbacConstant.SUPER_USER_ID));
        return new LoginUser(adminUser);
    }


    /**
     * logout
     */

    public void logout() {
        tokenService.logout();
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        applicationContext=event.getApplicationContext();
        //////////////////////////初始化插件子系统////////////////////////
        dao = event.getApplicationContext().getBean(Dao.class);
        Map<String, IServerPlugin> plugins = event.getApplicationContext().getBeansOfType(IServerPlugin.class);

        // Process the beans
        plugins.forEach((beanName, bean) -> {
            log.info("初始化plugin" + beanName);
            log.info("\t作者:" + bean.author());
            log.info("\t版本:", bean.version());
            bean.init(this);
        });
    }
}
