package com.global.config;

import com.global.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final AccountService accountService;
  private final DataSource dataSource;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .mvcMatchers("/", "/login", "/sign-up", "/check-email-token",
                     "/email-login", "/check-email-login", "/login-link")
        .permitAll()
        .mvcMatchers(HttpMethod.GET, "/profile/*")
        .permitAll()
        .anyRequest().authenticated();

    // permitAll()  <-- 로그인 안 한 상태에서나 로그인한 상태에서 모두 접근 가능함
    http.formLogin()
        .loginPage("/login").permitAll();

    // logoutSuccessUrl("/") <-- 로그아웃 하면 첫 페이지로 이동함
    http.logout()
        .logoutSuccessUrl("/");
    
    http.rememberMe()
      .userDetailsService(accountService)
      .tokenRepository(tokenRepository());
  }

  @Bean
  public PersistentTokenRepository tokenRepository(){
    JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
    jdbcTokenRepository.setDataSource(dataSource);
    return jdbcTokenRepository;
  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    // resource/static 에 있는 data 는 Spring Security 로 설정하지 않는 설정
    // resource/static/images 폴더에 있는 image 를 보이게 함
    web.ignoring()
       .mvcMatchers("/node_modules/**")
       .antMatchers("/favicon.ico", "/resources/**", "/error")
       .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
  }
}
