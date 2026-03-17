package cn.mapway.gwt_template.server.config.startup;

import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.client.repository.RepositoryFrame;
import cn.mapway.gwt_template.client.workspace.DevWorkspaceFrame;
import cn.mapway.gwt_template.server.config.AppConfig;
import cn.mapway.gwt_template.server.rbac.Permissions;
import cn.mapway.gwt_template.server.service.user.TokenService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.user.ResourcePoint;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.client.user.RbacFrame;
import cn.mapway.rbac.server.service.RbacResourceService;
import cn.mapway.rbac.shared.RbacConstant;
import cn.mapway.rbac.shared.RbacRole;
import cn.mapway.rbac.shared.ResourceKind;
import cn.mapway.rbac.shared.db.postgis.RbacResourceEntity;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import cn.mapway.server.IServerPlugin;
import cn.mapway.ui.client.IUserInfo;
import cn.mapway.ui.server.IServerContext;
import cn.mapway.ui.server.mvc.ServerModuleInfo;
import cn.mapway.ui.shared.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.resource.NutResource;
import org.nutz.resource.Scans;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ServerStartedOnce extends ApplicationObjectSupport implements IServerContext, ApplicationListener<ApplicationReadyEvent> {
    ApplicationContext applicationContext;
    Dao dao;
    ConcurrentHashMap<String, ConcurrentHashMap<String, Object>> sessionCacheGroup = new ConcurrentHashMap();
    @Resource
    TokenService tokenService;
    @Resource
    RbacResourceService rbacResourceService;


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
        applicationContext = event.getApplicationContext();
        AppConfig appConfig = applicationContext.getBean(AppConfig.class);
        log.info("[START] GIT ROOT 目录 {}", appConfig.getRepoRoot());

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

        //注册所有的功能点
        importAllModules();

        //确保一些目录存在
        if (Strings.isNotBlank(appConfig.getUploadRoot())) {
            Files.createDirIfNoExists(appConfig.getUploadRoot());
        }
        if (Strings.isNotBlank(appConfig.getRepoRoot())) {
            Files.createDirIfNoExists(appConfig.getRepoRoot());
        }
        if (Strings.isNotBlank(appConfig.getCertRoot())) {
            Files.createDirIfNoExists(appConfig.getCertRoot());
        }
        if (Strings.isNotBlank(appConfig.getProjectResRoot())) {
            Files.createDirIfNoExists(appConfig.getProjectResRoot());
        }
        //创建一些必要的账户
        //公共信箱帐号
        String userName = AppConstant.USER_BROADCAST_NAME;
        RbacUserEntity rbacUserEntity = dao.fetch(RbacUserEntity.class, Cnd.where(RbacUserEntity.FLD_USER_NAME, "=", userName));
        if (rbacUserEntity == null) {
            rbacUserEntity = new RbacUserEntity();
            rbacUserEntity.setToken(R.UU16());
            rbacUserEntity.setPassword("");
            rbacUserEntity.setCreateTime(new Date());
            rbacUserEntity.setUpdateTime(new Date());
            rbacUserEntity.setUserName(userName);
            rbacUserEntity.setConfig("");
            rbacUserEntity.setCreateBy("admin");
            rbacUserEntity.setAvatar("/img/avatar/broadcast.svg");
            rbacUserEntity.setDelFlag("");
            rbacUserEntity.setEmail(userName + "@cangling.cn");
            rbacUserEntity.setStatus("0");
            rbacUserEntity.setSex("");
            rbacUserEntity.setNickName(userName);
            rbacUserEntity.setUserType("00");
            rbacUserEntity.setRelId(AppConstant.USER_IS_PUBLIC_ACCOUNT);
            dao.insert(rbacUserEntity);
        }

    }

    private void importAllModules() {
        List<NutResource> nutResources = Scans.me().scan("static/js/app/allModules.json");
        if (nutResources.isEmpty()) {
            log.warn("not find preset tools json file in static/js/app/allModules.json");
            return;
        }
        try {
            String allModules = Streams.readAndClose(nutResources.get(0).getReader());
            List<ServerModuleInfo> modules = Json.fromJsonAsList(ServerModuleInfo.class, allModules);
            updateClientModuleResource(modules);
        } catch (IOException e) {
            log.error("import modules error", e.getMessage());
        }
    }

    /**
     * 更新系统前端模块资源
     *
     * @param modules
     */
    public void updateClientModuleResource(List<ServerModuleInfo> modules) {
        if (Lang.isEmpty(modules)) {
            log.warn("没有GWT模块定义");
            return;
        }
        List<RbacResourceEntity> resources = new ArrayList<>();
        for (ServerModuleInfo module : modules) {
            if (module.getTags().contains(CommonConstant.TAG_PREFERENCE)
                    || module.getTags().contains(CommonConstant.TAG_HIDDEN)
            ) {
                //设置不进入全局设置模块
                continue;
            }

            RbacResourceEntity resourceEntity = new RbacResourceEntity();
            resourceEntity.setResourceCode(module.code);
            resourceEntity.setName(module.name);
            resourceEntity.setKind(ResourceKind.RESOURCE_KIND_FUNCTION.getCode());
            resourceEntity.setSummary(module.summary);
            resourceEntity.setCatalog(ResourceKind.RESOURCE_KIND_FUNCTION.getName());
            resourceEntity.setData(module.hash);
            resourceEntity.setUnicodeIcon(module.unicode);
            resources.add(resourceEntity);
        }
        log.info("同步GWT模块定义 {}个", resources.size());
        rbacResourceService.clearAndReloadResourceByKind(ResourceKind.RESOURCE_KIND_FUNCTION, resources);

        //创建应用的资源点

        for (ResourcePoint point : ResourcePoint.values()) {
            RbacResourceEntity resourceEntity = new RbacResourceEntity();
            resourceEntity.setResourceCode(point.getCode());
            resourceEntity.setName(point.getName());
            resourceEntity.setCatalog(point.getCatalog());
            resourceEntity.setData(point.getData());
            resourceEntity.setUnicodeIcon(point.getUnicode());
            resourceEntity.setKind(point.getKind());
            rbacResourceService.confirmResourceExist(resourceEntity);
            log.info("[START] 确认资源点{}存在", point.getName());
        }

        //创建一个项目管理员角色
        BizResult<RbacRole> rbacRoleBizResult = rbacResourceService.confirmRoleExist(AppConstant.ROLE_SYS_PROJECT_MANAGER, "项目管理员", "", "", "");
        if (rbacRoleBizResult.isFailed()) {
            log.error("[START] 项目管理员角色 {}", rbacRoleBizResult.getMessage());
            throw Lang.makeThrow("[START]", rbacRoleBizResult.getMessage());
        }

        //系统消息管理角色
        rbacRoleBizResult = rbacResourceService.confirmRoleExist(AppConstant.ROLE_SYS_MESSAGE_MANAGER, "系统消息维护", "ROLE_SYS", "", "");
        if (rbacRoleBizResult.isFailed()) {
            log.error("[START] 系统消息管理角色 {}", rbacRoleBizResult.getMessage());
            throw Lang.makeThrow("[START]", rbacRoleBizResult.getMessage());
        }

        //系统LDAP管理角色
        rbacRoleBizResult = rbacResourceService.confirmRoleExist(AppConstant.ROLE_SYS_LDAP_MANAGER, "系统LDAP维护", "ROLE_SYS", "", "");
        if (rbacRoleBizResult.isFailed()) {
            log.error("[START] 系统LDAP管理角色 {}", rbacRoleBizResult.getMessage());
            throw Lang.makeThrow("[START]", rbacRoleBizResult.getMessage());
        }
        rbacResourceService.confirmResourceInRole("ldap_frame", AppConstant.ROLE_SYS_LDAP_MANAGER);


        //分配项目管理员 创建项目资源
        log.info("[START] 分配项目管理员 角色资源");
        rbacResourceService.confirmResourceInRole(ResourcePoint.RP_PROJECT_CREATE.getCode(), AppConstant.ROLE_SYS_PROJECT_MANAGER);
        rbacResourceService.confirmResourceInRole(ResourcePoint.RP_REPOSITORY_CREATE.getCode(), AppConstant.ROLE_SYS_PROJECT_MANAGER);

        //发布公共信箱权限
        log.info("[START] 创建发布公共信箱权限点");
        rbacResourceService.confirmResourceInRole(ResourcePoint.RP_MESSAGE_BROADCAST.getCode(), AppConstant.ROLE_SYS_MESSAGE_MANAGER);

        //系统管理员拥有组织管理权限
        log.info("[START] 分配系统管理员拥有　系统配置资源");
        rbacResourceService.confirmResourceInRole(RbacFrame.MODULE_CODE, RbacConstant.ROLE_SYS_MAINTAINER);

        //分配普通用户的权限
        log.info("[START] 分配普通用户拥有　资源");
        rbacResourceService.confirmResourceInRole(RepositoryFrame.MODULE_CODE, RbacConstant.ROLE_NORMAL_USER);
        rbacResourceService.confirmResourceInRole(DevWorkspaceFrame.MODULE_CODE, RbacConstant.ROLE_NORMAL_USER);
        rbacResourceService.confirmResourceInRole(ResourcePoint.RP_REPOSITORY_CREATE.getCode(), RbacConstant.ROLE_NORMAL_USER);


        //管理员拥有消息管理角色
        rbacResourceService.assignUserRole(String.valueOf(RbacConstant.SUPER_USER_ID), AppConstant.ROLE_SYS_MESSAGE_MANAGER, false);
        rbacResourceService.assignUserRole(String.valueOf(RbacConstant.SUPER_USER_ID), AppConstant.ROLE_SYS_LDAP_MANAGER, false);


    }
}
