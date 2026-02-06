package cn.mapway.gwt_template.server.config.security;

import cn.mapway.gwt_template.server.service.user.handler.AuthenticationEntryPointImpl;
import cn.mapway.gwt_template.server.service.user.login.LoginProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.web.filter.CorsFilter;

import javax.annotation.Resource;

/**
 * spring security配置
 *
 * @author cangling
 */
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    /**
     * 认证失败处理类
     */
    @Resource
    private AuthenticationEntryPointImpl unauthorizedHandler;
    @Resource
    private LoginProvider dynamicLdapProvider;
    /**
     * 跨域过滤器
     */
    @Resource
    private CorsFilter corsFilter;
    @Bean
    public HttpFirewall allowNonAsciiCharacters() {
        DefaultHttpFirewall firewall = new DefaultHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true); // Allow URL encoded characters
        return firewall;
    }
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // CRSF禁用，因为不使用session
                .csrf().disable()
                // 认证失败处理类
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                // 基于token，所以不需要session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS).and()
                // 过滤请求
                .authorizeRequests()
                // 对于登录login 验证码captchaImage 允许匿名访问
                .antMatchers("/js/**").permitAll()
                .antMatchers("/").permitAll()
                .antMatchers("/public/**").permitAll()
                .antMatchers("/preview").permitAll()
                .antMatchers("/socket/**").permitAll()
                .antMatchers("/terminal/**").permitAll()
                .antMatchers("/fileUpload").permitAll()
                .antMatchers("/upload/**").permitAll()
                .antMatchers("/rbac").permitAll()
                .antMatchers("/app").permitAll();
                httpSecurity.logout().logoutUrl("/logout").permitAll();

    }



}