package com.global.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .mvcMatchers("/", "/login", "/sign-up", "/check-email-token",
                     "/email-login", "/check-email-login", "/login-link")
        .permitAll()
        .mvcMatchers(HttpMethod.GET, "/profile/*")
        .permitAll()
        .anyRequest().authenticated();
  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    // resource/static 에 있는 data 는 Spring Security 로 설정하지 않는 설정
    // resource/static/images 폴더에 있는 image 를 보이게 함
    web.ignoring()
      .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
  }
}
