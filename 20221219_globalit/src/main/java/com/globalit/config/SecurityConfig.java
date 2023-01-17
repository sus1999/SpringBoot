package com.globalit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration /* 설정에 관련된 code 를 작성함 */
@EnableWebSecurity /* 웹 보안을 활성화함 */
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers("/", "/login", "/sign-up", "/check-email", "/check-email-token",
                        "/email-login", "/check-email-login", "/login-link")
                // ㄴ 인증 없이 접속 가능
                .permitAll()
                .mvcMatchers(HttpMethod.GET, "/profile/*")
                // ㄴ GET?
                .permitAll()
                .anyRequest()
                .authenticated();
    }
}
