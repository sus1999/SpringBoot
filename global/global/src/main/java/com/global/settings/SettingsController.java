package com.global.settings;

import com.global.account.AccountService;
import com.global.account.CurrentUser;
import com.global.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;


// error 가 없는 경우, update(수정작업) 를 진행함
// data 를 수정하는 경우에는 Service 에 위임해서
//                            ㄴ (이렇게 할려면 Service 클래스 타입의 멤버변수를 선언해야 함)
//                            멤버변수를 parameter 하는 생성자를 작성해서 Service 객체를 주입 받음
//                                 ㄴ @RequiredArgsConstructor 어노테이션을 사용함
//                            프로그래머가 code 를 작성해서 객체를 생성하지 않고
//                            필요한 객체를 Spring 이 자동으로 생성해서 할당까지 해줌
//                              ㄴ Inversion of Control (제어의 역행) : IoC
// Transaction 안에서 수정해야  DB 에 반영됨
// <-- @Transactional 어노테이션 사용


@Controller
@RequiredArgsConstructor
public class SettingsController {

  // PasswordFormValidator 를 Bean 으로 등록하지 않고
  // InitBinder 를 사용해서 객체를 생성함
  @InitBinder("passwordForm")
  public void initBinder(WebDataBinder webDataBinder){
    webDataBinder.addValidators(new PasswordFormValidator());
  }

  // "settings.profile" 문자열을 static 변수에 저장함
  static final String SETTINGS_PROFILE_VIEW = "settings/profile";
  static final String SETTINGS_PROFILE_URL = "/settings/profile";

  static final String SETTINGS_PASSWORD_VIEW = "settings/password";
  static final String SETTINGS_PASSWORD_URL = "/settings/password";

  // Service type 의 멤버변수 선언
  private final AccountService accountService;

  // 주소표시줄에
  // /settings/profile 요청이 들어오면
  // 자동으로 호출되는 메소드
  // @CurrentUser  <-- 현재 user(현재 login 상태에 있는 회원)
  //                   정보를 가져오기 위한 Annotation
  @GetMapping(SETTINGS_PROFILE_URL)
  public String updateProfileForm(@CurrentUser Account account, Model model){
    // model.addAttribute("account", account); 아래의 code 와 같은 기능을 함
    // attributeName 이 자동으로 "account" 라고 지어짐
    model.addAttribute(account);
    // model.addAttribute("profile", new Profile(account)); 아래의 code 와 같은 기능을 함
    // attributeName 이 자동으로 "profile" 이라고 지어짐
    model.addAttribute(new Profile(account));

    return SETTINGS_PROFILE_VIEW;

  }

  // post 방식으로 요청이 들어올 때
  // 자동으로 호출되는 메소드
  // @CurrentUser  <-- 현재 user(현재 login 상태에 있는 회원)
  //                   정보를 가져오기 위한 Annotation
  // @Valid @ModelAttribute Profile profile
  //   ㄴ form 에서 입력한 값들은 @ModelAttribute 를 사용해서 Profile 객체로 받아옴
  //   ㄴ @ModelAttribute 은 생략할 수 있음    ㄴ  @ModelAttribute 로 data 를 biding 함
  // Errors errors : @ModelAttribute 객체의 biding error 를 받아주는 객체
  //  ㄴ binding 하는데 error 가 있든지, validation 하는데 error 가 있는 경우 Errors 객체가 생성됨
  // @Valid @ModelAttribute Profile profile
  //  <-- Spring 이 Profile 객체를 자동으로 생성해서 (setter 를 사용해서) parameter 에 주입하는데
  //      이때, 기본 생성자를 호출함. Profile 클래스에 기본생성자를 작성해야 함
  @PostMapping(SETTINGS_PROFILE_URL)
  public String updateProfile(@CurrentUser Account account,
                              @Valid @ModelAttribute Profile profile,
                              Errors errors, Model model, RedirectAttributes redirectAttributes){

    // error 가 있는 경우 (validation 위반)
    // ㄴ model 에 form 에 채워진 data 가 자동으로 들어가고,
    //   error 에 대한 정보도 model 에 자동으로 들어감
    // ㄴ account 객체만 명시적으로 넣어주면 됨
    if(errors.hasErrors()){
      model.addAttribute(account);
      // 화면에는 현재 view 를 그대로 보여줌
      return SETTINGS_PROFILE_VIEW;
    }

    // error 가 없는 경우, update(수정작업) 를 진행함
    // data 를 수정하는 경우에는 Service 에 위임해서
    // Transaction 안에서 수정해야  DB 에 반영됨
    // <-- @Transactional 어노테이션 사용
    accountService.updateProfile(account, profile);
    redirectAttributes.addFlashAttribute("message", "프로필이 수정되었습니다.");

    // 수정한 후에는 redirect 로 root page 로 이동함
    //                            ㄴ  localhost:8080/
    //  SETTINGS_PROFILE_URL 변수에 / 가 이미 있으므로
    //  redirect: 뒤에는 / 가 없어야 함
    //  SETTINGS_PROFILE_URL = "/settings/profile"
    return "redirect:" + SETTINGS_PROFILE_URL;
  }

  @GetMapping(SETTINGS_PASSWORD_URL)
  public String updatePasswordForm(@CurrentUser Account account, Model model){
    model.addAttribute(account);

    // Form 으로 사용할 객체가 없음 -> Form 으로 사용할 클래스 작성하기
    // com.global.settings.PasswordForm 클래스 작성함
    model.addAttribute(new PasswordForm());

    return SETTINGS_PASSWORD_VIEW;
  }

  // @CurrentUser Account account : 현재 접속해 있는 사용자
  @PostMapping(SETTINGS_PASSWORD_URL)
  public String updatePassword(@CurrentUser Account account,
                               @Valid PasswordForm passwordForm,
                               Errors errors, Model model,
                               RedirectAttributes redirectAttributes){
    if(errors.hasErrors()){
      model.addAttribute(account);
      return SETTINGS_PASSWORD_VIEW;
    }

    accountService.updatePassword(account, passwordForm.getNewPassword());
    redirectAttributes.addFlashAttribute("message", "비밀번호를 수정했습니다.");

    return "redirect:" + SETTINGS_PASSWORD_URL;
  }

}
