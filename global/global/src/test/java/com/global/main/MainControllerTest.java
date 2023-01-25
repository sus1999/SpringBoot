package com.global.main;

import com.global.account.AccountRepository;
import com.global.account.AccountService;
import com.global.account.SignUpForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MainControllerTest {

  @Autowired
  MockMvc mockMvc;

  // AccountService 에 있는 method 를
  // 사용하기 위해서 의존성 주입 받음
  @Autowired
  AccountService accountService;

  // AccountRepository 에 있는 method 를
  // 사용하기 위해서 의존성 주입 받음
  @Autowired
  AccountRepository accountRepository;

  // @BeforeEach [test1] @AfterEach @BeforeEach [test2] @AfterEach @BeforeEach [test3] @AfterEach
  // @BeforeEach : 모든 test 를 실행할 때마다
  //               먼저 실행되는 부분을 의미함
  @BeforeEach
  void beforeEach(){
    // 아래의 정보에 해당하는 account 계정이 생성되고 저장됨
    // 이메일도 전송함
    SignUpForm signUpForm = new SignUpForm();
    signUpForm.setNickName("globaltest");
    signUpForm.setEmail("globaltest@gmail.com");
    signUpForm.setPassword("12345678");
    accountService.processNewAccount(signUpForm);
  }

  // @AfterEach : 모든 test 를 실행할 때마다
  //              나중에 실행되는 부분을 의미함
  // test 를 두 개 이상 실행할 때
  // @BeforeEach 에서 DB 에 넣은 내용이 중복되어서
  // 들어가게 되므로 @AfterEach 에서 삭제해 주어야 함
  //  ㄴ 삭제하려면 AccountRepository 가 필요함
  //      ㄴ  멤버변수에서 @Autowired 해 줌
  @AfterEach
  void afterEach(){
    // @BeforeEach 에서 DB 에 넣은 내용을 삭제함
    accountRepository.deleteAll();
  }


  @DisplayName("이메일로 로그인 성공 테스트")
  @Test
  void login_with_email() throws Exception{

    /*  @BeforeEach 에서 실행하는 부분 */

    // post 방식으로 요청(/login)을 보내면
    // Spring Security 가 로그인 처리를 해 줌
    // 이메일로 로그인하려고
    // param("username", "globaltest@gmail.com") 설정함
    // Spring Security 를 사용하면, csrf 라는 protection 이 활성화되어 있음
    //  .with(csrf())  <-- csrf token 을 같이 전송함
    // .andExpect(authenticated().withUsername("globaltest"))
    //   ㄴ globaltest 라는 username(nickName) 으로 인증됨
    // 이메일이 아니라, nickName으로 인증됨
    //  ㄴ UserAccount 클래스의 생성자에서
    //     super(account.getNickName(),  <-- 이렇게 설정했으므로...
    //             ㄴ username 부분을 nickName 으로 반환했음
    mockMvc.perform(post("/login")
           .param("username", "globaltest@gmail.com")
           .param("password", "12345678")
           .with(csrf()))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/"))
           .andExpect(authenticated().withUsername("globaltest"));
  }

  @DisplayName("nickName 으로 로그인 성공 테스트")
  @Test
  void login_with_nickName() throws Exception{

    /*  @BeforeEach 에서 실행하는 부분 */

    // post 방식으로 요청(/login)을 보내면
    // Spring Security 가 로그인 처리를 해 줌
    // 닉네임으로 로그인하려고
    // param("username", "globaltest") 설정함
    // Spring Security 를 사용하면, csrf 라는 protection 이 활성화되어 있음
    //  .with(csrf())  <-- csrf token 을 같이 전송함
    // .andExpect(authenticated().withUsername("globaltest"))
    //   ㄴ globaltest 라는 username(nickName) 으로 인증됨
    // 이메일이 아니라, nickName으로 인증됨
    //  ㄴ UserAccount 클래스의 생성자에서
    //     super(account.getNickName(),  <-- 이렇게 설정했으므로...
    //             ㄴ username 부분을 nickName 으로 반환했음
    mockMvc.perform(post("/login")
           .param("username", "globaltest")
           .param("password", "12345678")
           .with(csrf()))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/"))
           .andExpect(authenticated().withUsername("globaltest"));
  }

  @DisplayName("로그인 실패 테스트")
  @Test
  void login_fail() throws Exception{

    /*  @BeforeEach 에서 실행하는 부분 */

    // post 방식으로 요청(/login)을 보내면
    // Spring Security 가 로그인 처리를 해 줌
    // 로그인 실패를 테스트하려고
    // param("username", "11112222333") 설정함
    // param("password", "98764531") 설정함
    // Spring Security 를 사용하면, csrf 라는 protection 이 활성화되어 있음
    //  .with(csrf())  <-- csrf token 을 같이 전송함
    // .andExpect(authenticated().withUsername("globaltest"))
    //   ㄴ globaltest 라는 username(nickName) 으로 인증됨
    // 이메일이 아니라, nickName으로 인증됨
    //  ㄴ UserAccount 클래스의 생성자에서
    //     super(account.getNickName(),  <-- 이렇게 설정했으므로...
    //             ㄴ username 부분을 nickName 으로 반환했음
    // 실패하는 경우에는 redirect 가 /login?error 로 되도록 함
    //   ㄴ .andExpect(redirectedUrl("/login?error"))
    // 인증이 되지 않으므로...
    // .andExpect(unauthenticated()) 로 설정함
    mockMvc.perform(post("/login")
           .param("username", "11112222333")
           .param("password", "98764531")
           .with(csrf()))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/login?error"))
           .andExpect(unauthenticated());
  }

  @WithMockUser
  @DisplayName("로그아웃 테스트")
  @Test
  void logout() throws Exception{
    mockMvc.perform(post("/logout")
           .param("username", "11112222333")
           .param("password", "98764531")
           .with(csrf()))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/"))
           .andExpect(unauthenticated());
  }

}