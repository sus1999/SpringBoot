package com.global.account;

import com.global.domain.Account;
import com.global.settings.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.el.ELContext;
import javax.validation.Valid;
import java.util.List;

// AccountService 클래스는 @Service 에 의해서
// Bean 으로 등록되어 있음 (Bean : Spring 이 자동으로 생성하고 관리하는 객체)
//  ㄴ UserDetailsService 를 상속하는 Bean 이 하나만 있으면
//     Spring Security 에 별도로 설정하지 않아도 됨
//       ㄴ Spring 이 자동으로 이  Bean (AccountService) 을 사용함
//                 ㄴ login, logout 모두 자동으로 동작함
// @Transactional : AccountService 클래스의 모든 method 의 작업이
//                  Transaction 안에서 진행되도록 설정함
@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {
  private final AccountRepository accountRepository;
  private final JavaMailSender javaMailSender;
  private final PasswordEncoder passwordEncoder;
  // private final AuthenticationManager authenticationManager;

  
  public Account processNewAccount(SignUpForm signUpForm) {
    Account newAccount = saveNewAccount(signUpForm);
    // 이메일 보내기 전에 토큰값 생성하기
    newAccount.generateEmailCheckToken();
    sendSignUpConfirmEmail(newAccount);
    return newAccount;
  }

  private Account saveNewAccount(@Valid SignUpForm signUpForm) {
    Account account = Account.builder()
      .email(signUpForm.getEmail())
      .nickName(signUpForm.getNickName())
      .password(passwordEncoder.encode(signUpForm.getPassword()))
      .studyCreateByWeb(true)
      .studyEnrollmentResultByWeb(true)
      .studyUpdateByWeb(true)
      .build();

    Account newAccount = accountRepository.save(account);
    return newAccount;
  }

  public void sendSignUpConfirmEmail(Account newAccount) {
    SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
    // 토큰값에 해당하는 이메일 주소 받기
    simpleMailMessage.setTo(newAccount.getEmail());
    // 이메일 제목
    simpleMailMessage.setSubject("회원 가입 인증");
    // 이메일 본문
    // simpleMailMessage.setText("/check-email-token?token=이메일보내기전에생성한토큰값&email=토큰값에해당하는이메일주소");
    simpleMailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken()
      + "&email=" + newAccount.getEmail());
    javaMailSender.send(simpleMailMessage);
  }

  // password 를 encoding 하기 때문에 아래의 방법으로 로그인함
  public void login(Account account) {
    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                                                        // account.getNickName(),
                                                        new UserAccount(account),
                                                        account.getPassword(),
                                                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                                                      );
    // Spring 에서 제공하는 SecurityContextHolder 에서
    // context 를 받아와서 인증하기
    SecurityContext context = SecurityContextHolder.getContext();
    context.setAuthentication(token);
  }

  // readOnly = true : data 를 변경하는 것이 아니고, 로그인 할 때 확인만 함
  @Transactional(readOnly = true)
  @Override
  public UserDetails loadUserByUsername(String emailOrNickName) throws UsernameNotFoundException {
    // findByEmail() 로 email 로 로그인하는지 알아보기
    Account account = accountRepository.findByEmail(emailOrNickName);
    // email 로 로그인하는 것이 아니라면
    // findByNickName() 로 nickName 으로 로그인하는지 알아보기
    if(account == null){
      account = accountRepository.findByNickName(emailOrNickName);
    }

    // nickName 으로도 로그인하는 것이 아니라면
    // UsernameNotFoundException 예외를 발생시킴
    //   ㄴ email 또는 password 가 잘못되었다고 메세지를 return 함
    if(account == null){
      throw new UsernameNotFoundException(emailOrNickName);
    }

    // 현재 입력한 email 이나 nickName 에 해당하는 user 가 있는 경우
    // ㄴ Principal 에 해당하는 객체를 넘김
    //      ㄴ Spring Security 가 제공하는 User 를 상속하는 UserAccount 객체
    //                              com.global.account 패키지에 만들어 놓았음
    return new UserAccount(account);
  }

  //   - Entity 객체 변경은 반드시 Transaction 안에서 해야 함
  //     ㄴ Transaction 종료 직전이나 필요한 시점에 변경 사항을 DB 에 반영할 수 있기 때문
  // 데이터를 변경하는 작업을 하므로 @Transactional(readOnly = true) 로 설정하지 않음
  public void completeSignUp(Account account) {
    account.completeSignUp();
    login(account);
  }

  public void updateProfile(Account account, Profile profile) {
    // account 의 정보를 변경함
    // account 객체 <-- 현재 로그인해 있는 회원의 정보를 저장한 객체
    // profile 객체 <-- 변경한 내용(bio, url, occupation, location) 정보를 저장한 객체
    // 변경한 내용을 저장한 profile 객체에서 bio, url, occupation, location 을 갖고 와서
    // 현재 로그인한 회원의 정보를 저장하고 있는 account 객체의 bio, url, occupation, location 에 할당함
    account.setBio(profile.getBio());
    account.setUrl(profile.getUrl());
    account.setOccupation(profile.getOccupation());
    account.setLocation(profile.getLocation());

    // 프로필 사진 업데이트 처리 : 이미지를 가져와서 넣어줌
    account.setProfileImage(profile.getProfileImage());

    // account 객체의 멤버변수 값이 변경된 것을 DB 에도 반영함
    accountRepository.save(account);
  }

  // SettingsController 의 public String updatePassword() 메소드에서 호출함
  public void updatePassword(Account account, String newPassword) {
    // account 의 비밀번호를 새로운 비밀번호로 변경함
    // SettingsController 의 public String updatePassword() 메소드에서 전달받은
    // account 객체는 Detached 상태이지 Persistence 상태가 아님
    // 변경이력이 취합되지 않아서, code 로 명시적으로 merge 해 주어야 함
    // account.setPassword(newPassword); <-- encoding 되지 않은 상태로 저장됨
    // encoding 해서 저장하기
    account.setPassword(passwordEncoder.encode(newPassword));
    // 명시적으로 merge 해 주어야 함
    accountRepository.save(account);

  }
}
